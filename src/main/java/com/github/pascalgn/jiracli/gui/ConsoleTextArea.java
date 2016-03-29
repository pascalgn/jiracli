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
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
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

class ConsoleTextArea extends JTextArea {
    private static final long serialVersionUID = -8193770562227282747L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleTextArea.class);

    private static final String FONT_SIZE = "fontSize";
    private static final int MIN_FONT_SIZE = 6;
    private static final int MAX_FONT_SIZE = 72;

    private final BlockingQueue<String> input;
    private final ContextMenu contextMenu;

    private final Preferences preferences;

    private transient Integer editStart;

    public ConsoleTextArea() {
        this(0, 0);
    }

    public ConsoleTextArea(int rows, int columns) {
        super(rows, columns);
        input = new LinkedBlockingQueue<String>();

        contextMenu = new ContextMenu();

        preferences = Constants.getPreferences();

        int size = normalize(preferences.getInt(FONT_SIZE, getFont().getSize()));

        setCaret(new BlockCaret());
        setFont(new Font(Font.MONOSPACED, 0, size));
        setLineWrap(true);

        setEditable(false);

        setComponentPopupMenu(contextMenu);

        Object enterActionKey = getInputMap(JComponent.WHEN_FOCUSED).get(KeyStroke.getKeyStroke("ENTER"));
        getActionMap().put(enterActionKey, new EnterAction());

        Object escapeActionKey = "escape-action";
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ESCAPE"), escapeActionKey);
        getActionMap().put(escapeActionKey, new EscapeAction());

        addMouseWheelListener(new ZoomListener());

        Document doc = getDocument();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).setDocumentFilter(new DocFilter());
        }
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

        if (EventQueue.isDispatchThread()) {
            append(str);
            if (scrollBar != null) {
                scrollBar.setValue(scrollBar.getMaximum());
            }
        } else {
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        append(str);
                        if (scrollBar != null) {
                            scrollBar.setValue(scrollBar.getMaximum());
                        }
                    }
                });
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public String readLine() {
        if (EventQueue.isDispatchThread()) {
            throw new IllegalStateException("Method must not be called on EDT!");
        }
        String str = input.poll();
        if (str == null) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEditable(true);
                }
            });
            try {
                str = input.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return str;
    }

    public void setNewWindowListener(Runnable newWindowListener) {
        contextMenu.setNewWindowListener(newWindowListener);
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
            if (editStart != null) {
                Document doc = getDocument();
                String str;
                try {
                    str = doc.getText(editStart, doc.getLength() - editStart);
                    input.add(str);
                    doc.insertString(doc.getLength(), "\n", null);
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
