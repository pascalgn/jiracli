/**
 * Copyright 2016 Pascal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pascalgn.jiracli.gui;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.Constants;
import com.github.pascalgn.jiracli.command.CommandFactory;
import com.github.pascalgn.jiracli.util.RuntimeInterruptedException;

class ConsoleTextArea extends JTextArea {
    private static final long serialVersionUID = -8193770562227282747L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleTextArea.class);

    private static final String FONT_SIZE = "fontSize";
    private static final int MIN_FONT_SIZE = 6;
    private static final int MAX_FONT_SIZE = 72;

    private static final Object EOF = new Object();
    private static final Object INTERRUPT = new Object();

    private final BlockingQueue<Object> input;
    private final ContextMenu contextMenu;

    private final Preferences preferences;

    private final List<String> commands;

    private transient Runnable interruptListener;

    private transient Integer editStart;

    private transient volatile boolean readCommand;
    private transient volatile int commandIndex;
    private transient volatile String currentLine;

    public ConsoleTextArea() {
        this(0, 0);
    }

    public ConsoleTextArea(int rows, int columns) {
        super(rows, columns);
        input = new LinkedBlockingQueue<>();

        contextMenu = new ContextMenu();

        preferences = Constants.getPreferences();

        commands = new ArrayList<String>();

        int size = normalize(preferences.getInt(FONT_SIZE, getFont().getSize()));

        setCaret(new BlockCaret());
        setFont(new Font(Font.MONOSPACED, 0, size));
        setLineWrap(true);

        setEditable(false);

        setComponentPopupMenu(contextMenu);

        Object enterActionKey = getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("ENTER"));
        getActionMap().put(enterActionKey, new EnterAction());

        registerAction("escape-action", KeyStroke.getKeyStroke("ESCAPE"), new EscapeAction());
        registerAction("up-action", KeyStroke.getKeyStroke("UP"), new UpAction());
        registerAction("down-action", KeyStroke.getKeyStroke("DOWN"), new DownAction());
        registerAction("tab-action", KeyStroke.getKeyStroke("TAB"), new TabAction());
        registerAction("eof-action", KeyStroke.getKeyStroke("control D"), new EofAction());
        registerAction("interrupt-action", KeyStroke.getKeyStroke("control G"), new InterruptAction());

        addMouseWheelListener(new ZoomListener());

        Document doc = getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).setDocumentFilter(new DocFilter());
        }
    }

    private void registerAction(Object actionKey, KeyStroke keyStroke, Action action) {
        getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, actionKey);
        getActionMap().put(actionKey, action);
    }

    private static int normalize(int size) {
        if (size < MIN_FONT_SIZE) {
            return MIN_FONT_SIZE;
        } else if (size > MAX_FONT_SIZE) {
            return MAX_FONT_SIZE;
        }
        return size;
    }

    public void appendText(final String str) {
        if (!EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Method must be called on EDT!");
        }

        final JScrollBar scrollBar;
        JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
        if (scrollPane == null) {
            scrollBar = null;
        } else {
            JScrollBar sb = scrollPane.getHorizontalScrollBar();
            if (sb.getValue() == sb.getMaximum()) {
                scrollBar = sb;
            } else {
                scrollBar = null;
            }
        }
        append(str);
        if (scrollBar != null) {
            scrollBar.setValue(scrollBar.getMaximum());
        }
    }

    public String readCommand() {
        String command;
        readCommand = true;
        try {
            commandIndex = commands.size();
            command = readLine();
        } finally {
            readCommand = false;
        }
        if (command != null && !command.isEmpty()) {
            commands.add(command);
        }
        return command;
    }

    public String readLine() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Method must not be called on EDT!");
        }
        Object obj = input.poll();
        if (obj == null) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEditable(true);
                }
            });
            try {
                obj = input.take();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }
        }
        if (obj == EOF) {
            return null;
        } else if (obj == INTERRUPT) {
            throw new RuntimeInterruptedException(new InterruptedException());
        }
        return obj.toString();
    }

    public List<String> readLines() {
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = readLine()) != null) {
            if (line.equals(".")) {
                break;
            } else if (line.startsWith(".")) {
                lines.add(line.substring(1));
            } else {
                lines.add(line);
            }
        }
        return lines;
    }

    public void setNewWindowListener(Runnable newWindowListener) {
        contextMenu.setNewWindowListener(newWindowListener);
    }

    public void setInterruptListener(Runnable interruptListener) {
        this.interruptListener = interruptListener;
    }

    @Override
    public void setEditable(boolean editable) {
        if (editable == isEditable()) {
            return;
        }
        if (getDocument() != null) {
            if (editable) {
                editStart = getDocument().getLength();
            } else {
                editStart = null;
            }
        }
        if (getCaret() != null) {
            if (editable && getDocument() != null) {
                getCaret().setDot(getDocument().getLength());
            }
            getCaret().setVisible(editable && isFocusOwner());
        }
        super.setEditable(editable);
    }

    private class EnterAction extends AbstractAction {
        private static final long serialVersionUID = 7344929003097951487L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable()) {
                Document doc = getDocument();
                String str;
                try {
                    str = doc.getText(editStart, doc.getLength() - editStart);
                    doc.insertString(doc.getLength(), "\n", null);
                    input.add(str);
                } catch (BadLocationException e) {
                    LOGGER.trace("Invalid location!", e);
                }
                setEditable(false);
            }
        }
    }

    private class EscapeAction extends AbstractAction {
        private static final long serialVersionUID = -6308039227634565864L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (isEditable()) {
                setCaretPosition(getDocument().getLength());
            }
        }
    }

    private class UpAction extends AbstractAction {
        private static final long serialVersionUID = 4165357229398455227L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable() && readCommand && !commands.isEmpty()) {
                AbstractDocument doc = (AbstractDocument) getDocument();
                if (commandIndex > 0) {
                    if (commandIndex == commands.size()) {
                        try {
                            currentLine = doc.getText(editStart, doc.getLength() - editStart);
                        } catch (BadLocationException e) {
                            LOGGER.trace("BadLocationException", e);
                        }
                    }
                    --commandIndex;
                    String command = commands.get(commandIndex);
                    try {
                        doc.replace(editStart, doc.getLength() - editStart, command, null);
                    } catch (BadLocationException e) {
                        LOGGER.trace("BadLocationException", e);
                    }
                }
            }
        }
    }

    private class DownAction extends AbstractAction {
        private static final long serialVersionUID = -4836920021641673079L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable() && readCommand && !commands.isEmpty()) {
                AbstractDocument doc = (AbstractDocument) getDocument();
                if (commandIndex < commands.size()) {
                    ++commandIndex;
                    String command;
                    if (commandIndex == commands.size()) {
                        command = currentLine;
                    } else {
                        command = commands.get(commandIndex);
                    }
                    try {
                        doc.replace(editStart, doc.getLength() - editStart, command, null);
                    } catch (BadLocationException e) {
                        LOGGER.trace("BadLocationException", e);
                    }
                }
            }
        }
    }

    private class TabAction extends AbstractAction {
        private static final long serialVersionUID = 8588600080225765686L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable() && readCommand) {
                AbstractDocument doc = (AbstractDocument) getDocument();
                String current;
                try {
                    current = doc.getText(editStart, doc.getLength() - editStart);
                } catch (BadLocationException e) {
                    LOGGER.trace("BadLocationException", e);
                    return;
                }
                int index = current.lastIndexOf("|");
                if (index >= 0 && index < current.length()) {
                    current = current.substring(index + 1);
                }
                current = current.trim();
                List<String> commands = CommandFactory.getInstance().getCommandNames();
                String match = null;
                for (String commandName : commands) {
                    if (commandName.startsWith(current)) {
                        if (match == null) {
                            match = commandName;
                        } else {
                            // ambiguous command
                            match = null;
                            break;
                        }
                    }
                }
                if (match != null) {
                    try {
                        doc.insertString(doc.getLength(), match.substring(current.length()) + " ", null);
                    } catch (BadLocationException e) {
                        LOGGER.trace("BadLocationException", e);
                    }
                }
            }
        }
    }

    private class EofAction extends AbstractAction {
        private static final long serialVersionUID = -7067756446670990814L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable()) {
                AbstractDocument doc = (AbstractDocument) getDocument();
                String line;
                try {
                    line = doc.getText(editStart, doc.getLength() - editStart);
                } catch (BadLocationException e) {
                    LOGGER.trace("BadLocationException", e);
                    return;
                }
                if (line.isEmpty()) {
                    try {
                        doc.insertString(doc.getLength(), "\n", null);
                    } catch (BadLocationException e) {
                        LOGGER.trace("BadLocationException", e);
                    }
                    setEditable(false);
                    input.add(EOF);
                }
            }
        }
    }

    private class InterruptAction extends AbstractAction {
        private static final long serialVersionUID = -4180083511703828311L;

        @Override
        public void actionPerformed(ActionEvent evt) {
            if (isEditable()) {
                setEditable(false);
                input.add(INTERRUPT);
            } else {
                Runnable listener = interruptListener;
                if (listener != null) {
                    Thread t = new Thread(listener);
                    t.setName("call-interrupt-listener");
                    t.setDaemon(true);
                    t.start();
                }
            }
        }
    }

    private class DocFilter extends DocumentFilter {
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (editStart == null || offset >= editStart) {
                super.remove(fb, offset, length);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (editStart == null || offset >= editStart) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    private class ZoomListener extends MouseAdapter {
        @Override
        public void mouseWheelMoved(MouseWheelEvent evt) {
            if (evt.isControlDown() && !evt.isShiftDown() && !evt.isAltDown() && !evt.isAltGraphDown()
                    && !evt.isMetaDown()) {
                Font font = getFont();
                int newSize = font.getSize() + (evt.getWheelRotation() < 0 ? 3 : -3);
                newSize = normalize(newSize);
                setFont(font.deriveFont((float) newSize));
                preferences.putInt(FONT_SIZE, newSize);
            } else {
                evt.getComponent().getParent().dispatchEvent(evt);
            }
        }
    }
}
