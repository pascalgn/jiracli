/*
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

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

class BlockCaret extends DefaultCaret {
    private static final long serialVersionUID = -5486657861892088798L;

    @Override
    protected synchronized void damage(Rectangle rectangle) {
        if (rectangle == null) {
            return;
        }

        x = rectangle.x;
        y = rectangle.y;
        height = rectangle.height;
        if (width <= 0) {
            width = getComponent().getWidth();
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        JTextComponent comp = getComponent();
        if (comp == null) {
            return;
        }

        int dot = getDot();

        Rectangle rectangle;
        char dotChar;
        try {
            rectangle = comp.modelToView(dot);
            if (rectangle == null) {
                return;
            }
            dotChar = comp.getText(dot, 1).charAt(0);
        } catch (BadLocationException e) {
            return;
        }

        if (dotChar == '\r' || dotChar == '\n') {
            dotChar = ' ';
        }

        if ((x != rectangle.x) || (y != rectangle.y)) {
            repaint();
            x = rectangle.x;
            y = rectangle.y;
            height = rectangle.height;
        }

        g.setColor(comp.getCaretColor());
        g.setXORMode(comp.getBackground());

        width = g.getFontMetrics().charWidth(dotChar);
        if (width <= 0) {
            width = g.getFontMetrics().charWidth(' ');
        }

        if (isVisible()) {
            g.fillRect(rectangle.x, rectangle.y, width, rectangle.height);
        }
    }
}
