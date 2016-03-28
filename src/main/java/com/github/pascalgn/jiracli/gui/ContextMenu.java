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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

class ContextMenu extends JPopupMenu {
    private static final long serialVersionUID = 7288490759713985256L;

    private Runnable newWindowListener;

    public ContextMenu() {
        newWindowListener = new Runnable() {
            @Override
            public void run() {
            }
        };
        initComponents();
    }

    private void initComponents() {
        JMenuItem newWindow = new JMenuItem("New window");
        newWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newWindowListener.run();
            }
        });
        add(newWindow);
    }

    public void setNewWindowListener(Runnable newWindowListener) {
        this.newWindowListener = newWindowListener;
    }
}
