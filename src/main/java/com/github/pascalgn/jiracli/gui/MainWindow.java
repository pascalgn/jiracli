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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import com.github.pascalgn.jiracli.Constants;

/**
 * Main window of the GUI: Displays the console for input/output
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = -6569821157912403607L;

    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    private static final int MIN_SIZE = 200;

    private final ConsoleTextArea consoleTextArea;
    private final Preferences preferences;

    private Runnable newWindowListener;

    public MainWindow() {
        super(Constants.getTitle());
        setIconImages(Images.getIcons());

        newWindowListener = new Runnable() {
            @Override
            public void run() {
            }
        };

        consoleTextArea = new ConsoleTextArea(25, 80);
        consoleTextArea.setNewWindowListener(new Runnable() {
            @Override
            public void run() {
                newWindowListener.run();
            }
        });

        preferences = Constants.getPreferences();

        JScrollPane consoleTextAreaScroll = new JScrollPane(consoleTextArea);
        consoleTextAreaScroll.setBorder(null);
        consoleTextAreaScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(consoleTextAreaScroll);

        Object ctrlNActionKey = "ctrl-n-action";
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control N"),
                ctrlNActionKey);
        getRootPane().getActionMap().put(ctrlNActionKey, new AbstractAction() {
            private static final long serialVersionUID = 5667457277890114769L;

            @Override
            public void actionPerformed(ActionEvent e) {
                newWindowListener.run();
            }
        });

        int width = preferences.getInt(WIDTH, -1);
        int height = preferences.getInt(HEIGHT, -1);
        if (width == -1 || height == -1) {
            pack();
        } else {
            width = normalize(width);
            height = normalize(height);
            setSize(width, height);
        }

        addComponentListener(new ResizeListener());

        setLocationRelativeTo(null);
    }

    private static int normalize(int size) {
        return (size < MIN_SIZE ? MIN_SIZE : size);
    }

    public void appendText(String str) {
        consoleTextArea.appendText(str);
    }

    public String readLine() {
        return consoleTextArea.readLine();
    }

    public void setNewWindowListener(Runnable newWindowListener) {
        this.newWindowListener = newWindowListener;
    }

    private class ResizeListener extends ComponentAdapter {
        @Override
        public void componentResized(ComponentEvent e) {
            preferences.putInt(WIDTH, getWidth());
            preferences.putInt(HEIGHT, getHeight());
        }
    }
}
