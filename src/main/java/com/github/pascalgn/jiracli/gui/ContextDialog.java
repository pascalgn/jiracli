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

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

public class ContextDialog extends JFrame {
    private static final long serialVersionUID = 8255092098159010094L;

    private final ContextPanel contextPanel;

    private Runnable cancelListener;

    public ContextDialog(String givenRootURL, String givenUsername) {
        super("Jiracli 1.0.1-SNAPSHOT");
        setIconImages(Images.getIcons());

        contextPanel = new ContextPanel(givenRootURL, givenUsername);
        contextPanel.setCancelListener(new Runnable() {
            @Override
            public void run() {
                cancelListener.run();
            }
        });

        cancelListener = new Runnable() {
            @Override
            public void run() {
                setVisible(false);
                dispose();
            }
        };

        Object escapeActionKey = "escape-action-key";
        JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), escapeActionKey);
        root.getActionMap().put(escapeActionKey, new AbstractAction() {
            private static final long serialVersionUID = 3618544318781289554L;

            @Override
            public void actionPerformed(ActionEvent e) {
                cancelListener.run();
            }
        });

        root.setDefaultButton(contextPanel.getOk());

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contextPanel);
        pack();
        setLocationRelativeTo(null);
    }

    public String getRootURL() {
        return contextPanel.getRootURL();
    }

    public String getUsername() {
        return contextPanel.getUsername();
    }

    public char[] getPassword() {
        return contextPanel.getPassword();
    }

    public void setOkListener(Runnable okListener) {
        contextPanel.setOkListener(okListener);
    }

    public void setCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }
}
