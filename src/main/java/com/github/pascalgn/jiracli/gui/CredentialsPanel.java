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

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.github.pascalgn.jiracli.Constants;
import com.github.pascalgn.jiracli.util.Credentials;

class CredentialsPanel extends JPanel {
    private static final long serialVersionUID = -7726038259989558546L;

    public static Credentials getCredentials(Window parent, String url) {
        CredentialsPanel credentialsPanel = new CredentialsPanel(url);
        int result = JOptionPane.showConfirmDialog(parent, credentialsPanel, Constants.getTitle(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = credentialsPanel.getUsername();
            char[] password = credentialsPanel.getPassword();
            if (username.trim().isEmpty()) {
                return null;
            } else {
                return new Credentials(username.trim(), password);
            }
        } else {
            return null;
        }
    }

    private final String url;

    private final JTextField username;
    private final JPasswordField password;

    private CredentialsPanel(String url) {
        this.url = url;
        username = new JTextField(20);
        password = new JPasswordField(20);
        layoutComponents();
    }

    private void layoutComponents() {
        JLabel titleLabel = new JLabel("<html>Please enter the credentials for <b>" + url + "</b>");

        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(titleLabel).addGroup(groupLayout
                .createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup().addComponent(usernameLabel).addComponent(passwordLabel))
                .addGroup(groupLayout.createParallelGroup()
                        .addComponent(username, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                        .addComponent(password, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                                GroupLayout.PREFERRED_SIZE))));
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addComponent(titleLabel)
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(usernameLabel).addComponent(username))
                .addGroup(groupLayout.createBaselineGroup(true, false).addComponent(passwordLabel)
                        .addComponent(password)));
        setLayout(groupLayout);
    }

    private String getUsername() {
        return username.getText();
    }

    private char[] getPassword() {
        return password.getPassword();
    }
}
