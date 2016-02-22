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

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

class Panel extends JPanel {
    private static final long serialVersionUID = 2206865626322221745L;

    private final JTextField rootURL;
    private final JTextField username;

    private final ConsoleTextArea consoleTextArea;

    public Panel(String rootURL, String username) {
        this.rootURL = new JTextField(rootURL, 20);
        this.username = new JTextField(username, 10);
        this.consoleTextArea = new ConsoleTextArea(20, 80);
        layoutComponents();
    }

    private void layoutComponents() {
        JLabel rootURLLabel = new JLabel("Root URL:");

        JLabel usernameLabel = new JLabel("Username:");

        JSeparator separator = new JSeparator();

        JScrollPane consoleScrollPane = new JScrollPane(consoleTextArea);
        consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        GroupLayout groupLayout = new GroupLayout(this);
        groupLayout.setAutoCreateContainerGaps(true);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createParallelGroup().addComponent(rootURLLabel).addComponent(usernameLabel))
                .addGroup(groupLayout.createParallelGroup()
                        .addGroup(groupLayout.createSequentialGroup().addComponent(rootURL))
                        .addGroup(groupLayout.createSequentialGroup().addComponent(username))))
                .addComponent(separator).addComponent(consoleScrollPane));
        groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                .addGroup(groupLayout.createBaselineGroup(true, false).addComponent(rootURLLabel).addComponent(rootURL))
                .addGroup(
                        groupLayout.createBaselineGroup(true, false).addComponent(usernameLabel).addComponent(username))
                .addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE)
                .addComponent(consoleScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.PREFERRED_SIZE));
        setLayout(groupLayout);
    }

    public void appendText(String str) {
        consoleTextArea.appendText(str);
    }

    public String readLine() {
        return consoleTextArea.readLine();
    }
}
