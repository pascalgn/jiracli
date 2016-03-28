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
import java.awt.event.ActionListener;
import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class ConsoleTextAreaDemo extends JFrame {
    private static final long serialVersionUID = -509073864020457352L;

    public ConsoleTextAreaDemo() {
        super(ConsoleTextAreaDemo.class.getSimpleName());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new Panel());
        pack();
        setLocationRelativeTo(null);
    }

    private static class Panel extends JPanel {
        private static final long serialVersionUID = 8103121156068220256L;

        private final JTextArea appendTextInput;
        private final JButton appendText;

        private final JButton readLine;
        private final JTextArea readLineOutput;

        private final ConsoleTextArea consoleText;

        public Panel() {
            appendTextInput = new JTextArea(3, 40);
            appendText = new JButton("appendText");

            readLine = new JButton("readLine");
            readLineOutput = new JTextArea(3, 40);

            consoleText = new ConsoleTextArea(10, 40);

            initComponents();
            layoutComponents();
        }

        private void initComponents() {
            appendText.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    consoleText.appendText(appendTextInput.getText());
                }
            });

            readLine.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    readLineOutput.setText("");
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readLine();
                        }
                    });
                    thread.setDaemon(true);
                    thread.start();
                }
            });
        }

        private void readLine() {
            String str = consoleText.readLine();
            readLineOutput.setText(Objects.toString(str));
        }

        private void layoutComponents() {
            JScrollPane printTextScrollPane = new JScrollPane(appendTextInput);
            printTextScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            JSeparator separator1 = new JSeparator();

            JScrollPane readLineTextScrollPane = new JScrollPane(readLineOutput);
            readLineTextScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            JSeparator separator2 = new JSeparator();

            JScrollPane consoleScrollPane = new JScrollPane(consoleText);
            consoleScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            GroupLayout groupLayout = new GroupLayout(this);
            groupLayout.setAutoCreateContainerGaps(true);
            groupLayout.setAutoCreateGaps(true);
            groupLayout.setHorizontalGroup(groupLayout.createParallelGroup().addComponent(printTextScrollPane)
                    .addComponent(appendText, Alignment.TRAILING).addComponent(separator1)
                    .addComponent(readLine, Alignment.TRAILING).addComponent(readLineTextScrollPane)
                    .addComponent(separator2).addComponent(consoleScrollPane));

            groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                    .addComponent(printTextScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                    .addComponent(appendText)
                    .addComponent(separator1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                    .addComponent(readLine)
                    .addComponent(readLineTextScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                    .addComponent(separator2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                            GroupLayout.PREFERRED_SIZE)
                    .addComponent(consoleScrollPane));
            setLayout(groupLayout);
        }
    }

    public static void main(String[] args) {
        new ConsoleTextAreaDemo().setVisible(true);
    }
}
