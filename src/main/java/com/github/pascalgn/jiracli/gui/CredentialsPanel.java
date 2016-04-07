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

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.github.pascalgn.jiracli.Constants;
import com.github.pascalgn.jiracli.util.Credentials;

class CredentialsPanel extends JPanel {
    private static final long serialVersionUID = -7726038259989558546L;

    public static Credentials getCredentials(Window parent, String username, String url) {
        CredentialsPanel credentialsPanel = new CredentialsPanel(username, url);
        int result = JOptionPane.showConfirmDialog(parent, credentialsPanel, Constants.getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            if (credentialsPanel.isAnonymous()) {
                return Credentials.getAnonymous();
            } else {
                String user = credentialsPanel.getUsername();
                char[] password = credentialsPanel.getPassword();
                return Credentials.create(user, password);
            }
        } else {
            return null;
        }
    }

    private final String url;

    private final JRadioButton usernamePassword;
    private final JRadioButton anonymous;
    private final ButtonGroup buttonGroup;

    private final JLabel usernameLabel;
    private final JTextField username;

    private final JLabel passwordLabel;
    private final JPasswordField password;

    private CredentialsPanel(String username, String url) {
        this.url = url;
        this.usernamePassword = new JRadioButton("Username and password");
        this.usernamePassword.addActionListener(new RadioButtonListener());
        this.anonymous = new JRadioButton("Anonymous access");
        this.anonymous.addActionListener(new RadioButtonListener());
        this.buttonGroup = new ButtonGroup();
        this.buttonGroup.add(usernamePassword);
        this.buttonGroup.add(anonymous);
        this.usernameLabel = new JLabel("Username:");
        this.username = new JTextField(Objects.toString(username, ""), 20);
        this.passwordLabel = new JLabel("Password:");
        this.password = new JPasswordField(20);
        if (username == null || username.isEmpty()) {
            this.anonymous.setSelected(true);
        } else {
            this.usernamePassword.setSelected(true);
            this.password.addAncestorListener(new RequestFocusListener());
        }
        radioButtonToggled();
        layoutComponents();
    }

    private void layoutComponents() {
        JLabel titleLabel = new JLabel("Please enter the credentials for " + url);

        int em = getFontMetrics(getFont()).stringWidth("m");

        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup()
                .addComponent(titleLabel)
                .addComponent(usernamePassword)
                .addGroup(groupLayout.createSequentialGroup().addGap(3 * em)
                        .addGroup(groupLayout.createParallelGroup().addComponent(usernameLabel)
                                .addComponent(passwordLabel))
                        .addGroup(groupLayout.createParallelGroup()
                                .addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.PREFERRED_SIZE)
                                .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                        GroupLayout.PREFERRED_SIZE)))
                .addComponent(anonymous));
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addComponent(titleLabel).addComponent(usernamePassword)
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(usernameLabel).addComponent(username))
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(passwordLabel).addComponent(password))
                .addComponent(anonymous));
        setLayout(groupLayout);
    }

    public boolean isAnonymous() {
        return anonymous.isSelected();
    }

    public String getUsername() {
        return username.getText().trim();
    }

    public char[] getPassword() {
        return password.getPassword();
    }

    private void radioButtonToggled() {
        boolean enabled = usernamePassword.isSelected();
        usernameLabel.setEnabled(enabled);
        username.setEnabled(enabled);
        passwordLabel.setEnabled(enabled);
        password.setEnabled(enabled);
    }

    private class RadioButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent evt) {
            radioButtonToggled();
        }
    }

    private static class RequestFocusListener implements AncestorListener {
        @Override
        public void ancestorAdded(AncestorEvent evt) {
            final JComponent component = evt.getComponent();
            component.requestFocusInWindow();
            // for macOS:
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    component.requestFocusInWindow();
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            component.requestFocusInWindow();
                        }
                    });
                }
            });
        }

        @Override
        public void ancestorRemoved(AncestorEvent evt) {
        }

        @Override
        public void ancestorMoved(AncestorEvent evt) {
        }
    }
}
