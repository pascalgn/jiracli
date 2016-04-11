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
package com.github.pascalgn.jiracli.command;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;

@CommandDescription(names = "clipboard", description = "Copy text from/to clipboard")
class Clipboard implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Clipboard.class);

    @Override
    public Data execute(Context context, Data input) {
        java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        Text text = input.toText();
        if (text == null) {
            Transferable contents = clipboard.getContents(this);

            Object transferData;
            try {
                transferData = contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException e) {
                LOGGER.trace("Could not get clipboard data", e);
                return None.getInstance();
            } catch (IOException e) {
                LOGGER.debug("Could not get clipboard data", e);
                return None.getInstance();
            }

            String str = Objects.toString(transferData, "");
            return new Text(str);
        } else {
            String str = text.getText();
            StringSelection stringSelection = new StringSelection(str);
            clipboard.setContents(stringSelection, stringSelection);
            return None.getInstance();
        }
    }
}
