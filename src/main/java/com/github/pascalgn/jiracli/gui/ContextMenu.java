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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import com.github.pascalgn.jiracli.util.Runnables;

class ContextMenu extends JPopupMenu {
    private static final long serialVersionUID = 7288490759713985256L;

    private Runnable newWindowListener;

    private Runnable increaseZoomListener;
    private Runnable decreaseZoomListener;
    private Runnable resetZoomListener;

    public ContextMenu() {
        newWindowListener = Runnables.empty();
        increaseZoomListener = Runnables.empty();
        decreaseZoomListener = Runnables.empty();
        resetZoomListener = Runnables.empty();
        initComponents();
    }

    private void initComponents() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        String modifier = (shortcut == InputEvent.META_MASK ? "meta" : "control");

        JMenuItem newWindow = new JMenuItem("New window");
        newWindow.setAccelerator(KeyStroke.getKeyStroke(modifier + " N"));
        newWindow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newWindowListener.run();
            }
        });
        add(newWindow);

        addSeparator();

        JMenuItem increaseZoom = new JMenuItem("Increase font size");
        increaseZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, shortcut));
        increaseZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                increaseZoomListener.run();
            }
        });
        add(increaseZoom);

        JMenuItem decreaseZoom = new JMenuItem("Decrease font size");
        decreaseZoom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, shortcut));
        decreaseZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                decreaseZoomListener.run();
            }
        });
        add(decreaseZoom);

        JMenuItem resetZoom = new JMenuItem("Reset font size");
        resetZoom.setAccelerator(KeyStroke.getKeyStroke(modifier + " 0"));
        resetZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetZoomListener.run();
            }
        });
        add(resetZoom);
    }

    public void setNewWindowListener(Runnable newWindowListener) {
        this.newWindowListener = newWindowListener;
    }

    public void setIncreaseZoomListener(Runnable increaseZoomListener) {
        this.increaseZoomListener = increaseZoomListener;
    }

    public void setDecreaseZoomListener(Runnable decreaseZoomListener) {
        this.decreaseZoomListener = decreaseZoomListener;
    }

    public void setResetZoomListener(Runnable resetZoomListener) {
        this.resetZoomListener = resetZoomListener;
    }
}
