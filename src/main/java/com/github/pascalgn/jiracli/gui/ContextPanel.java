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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

class ContextPanel extends JPanel {
    private static final long serialVersionUID = -7726038259989558546L;

    private final JTextField rootURL;
    private final JTextField username;
    private final JPasswordField password;
    private final JButton ok;
    private final JButton cancel;

    private final Color defaultForeground;

    private Runnable okListener;
    private Runnable cancelListener;

    public ContextPanel(String givenRootURL, String givenUsername) {
        rootURL = new JTextField(givenRootURL, 40);
        username = new JTextField(givenUsername, 20);
        password = new JPasswordField(20);
        ok = new JButton("OK");
        cancel = new JButton("Cancel");
        defaultForeground = rootURL.getForeground();
        okListener = new Runnable() {
            @Override
            public void run() {
            }
        };
        cancelListener = new Runnable() {
            @Override
            public void run() {
            }
        };
        initComponents();
        layoutComponents();
    }

    private void initComponents() {
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okListener.run();
            }
        });

        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelListener.run();
            }
        });

        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateRootURL();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validateRootURL();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validateRootURL();
            }
        };

        rootURL.getDocument().addDocumentListener(validationListener);

        validateRootURL();
    }

    private void validateRootURL() {
        boolean valid;
        String url = rootURL.getText();
        if (url.isEmpty()) {
            valid = false;
        } else {
            try {
                new URL(url);
                valid = true;
            } catch (MalformedURLException e) {
                valid = false;
            }
        }
        if (valid) {
            rootURL.setForeground(defaultForeground);
            ok.setEnabled(true);
        } else {
            rootURL.setForeground(Color.RED);
            ok.setEnabled(false);
        }
    }

    private void layoutComponents() {
        JLabel rootURLLabel = new JLabel("Root URL:");

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        JSeparator separator = new JSeparator();

        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                .addGroup(groupLayout
                        .createSequentialGroup()
                        .addGroup(groupLayout.createParallelGroup().addComponent(rootURLLabel)
                                .addComponent(usernameLabel).addComponent(passwordLabel))
                        .addGroup(groupLayout.createParallelGroup().addComponent(rootURL)
                                .addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.PREFERRED_SIZE)))
                .addComponent(separator).addGroup(Alignment.TRAILING,
                        groupLayout.createSequentialGroup().addComponent(cancel).addComponent(ok)));
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createBaselineGroup(true, false).addComponent(rootURLLabel).addComponent(rootURL))
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(usernameLabel).addComponent(username))
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(passwordLabel).addComponent(password))
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                .addGroup(groupLayout.createBaselineGroup(true, false).addComponent(cancel).addComponent(ok)));
        setLayout(groupLayout);
    }

    public JButton getOk() {
        return ok;
    }

    public String getRootURL() {
        return rootURL.getText();
    }

    public String getUsername() {
        return username.getText();
    }

    public char[] getPassword() {
        return password.getPassword();
    }

    public void setOkListener(Runnable okListener) {
        this.okListener = okListener;
    }

    public void setCancelListener(Runnable cancelListener) {
        this.cancelListener = cancelListener;
    }
}
