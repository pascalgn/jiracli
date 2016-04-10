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
package com.github.pascalgn.jiracli.context;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.util.RuntimeInterruptedException;

class EditorProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditorProvider.class);

    private static final String[] EMPTY = new String[0];

    private static final String EDITOR = "EDITOR";
    private static final String DEFAULT_EDITOR = "vim";
    private static final String[] CONSOLE_EDITORS = { "/usr/bin/vim", "/usr/bin/emacs", "/usr/bin/nano" };

    private static final String VISUAL = "VISUAL";
    private static final String DEFAULT_GUI_EDITOR = "gedit";
    private static final String[] GUI_EDITORS = { "/usr/bin/gedit", "/usr/bin/kate", "/usr/bin/kwrite",
            "/usr/bin/mousepad", "/usr/bin/gvim" };

    private static final EditorProviderDelegate EDITOR_PROVIDER = createEditorProvider();

    public static Editor getEditor(boolean gui) {
        if (gui) {
            return EDITOR_PROVIDER.getGUIEditor();
        } else {
            // We still need to check if we have a valid console for a text-based editor!
            Console console = System.console();
            return (console == null ? EDITOR_PROVIDER.getGUIEditor() : EDITOR_PROVIDER.getConsoleEditor());
        }
    }

    private static EditorProviderDelegate createEditorProvider() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")) {
            return new MacEditorProvider();
        } else if (os.contains("win")) {
            return new WindowsEditorProvider();
        } else {
            return new DefaultEditorProvider();
        }
    }

    private static interface EditorProviderDelegate {
        Editor getConsoleEditor();

        Editor getGUIEditor();
    }

    private static class WindowsEditorProvider implements EditorProviderDelegate {
        private static final String NOTEPAD_EXE = getEnv("WINDIR", "C:/Windows") + "/System32/notepad.exe";

        private final Editor consoleEditor;
        private final Editor guiEditor;

        public WindowsEditorProvider() {
            // Current Windows versions (Windows 10) don't have a console editor!
            consoleEditor = new EditorImpl(firstValidCommand(EDITOR, NOTEPAD_EXE, EMPTY));
            guiEditor = new EditorImpl(firstValidCommand(VISUAL, NOTEPAD_EXE, EMPTY));
        }

        private static String getEnv(String name, String defaultValue) {
            String value = System.getenv(name);
            return (value == null || value.trim().isEmpty() ? defaultValue : value);
        }

        @Override
        public Editor getConsoleEditor() {
            return consoleEditor;
        }

        @Override
        public Editor getGUIEditor() {
            return guiEditor;
        }
    }

    private static class MacEditorProvider implements EditorProviderDelegate {
        private Editor consoleEditor;
        private Editor guiEditor;

        public MacEditorProvider() {
            consoleEditor = new EditorImpl(firstValidCommand(EDITOR, DEFAULT_EDITOR, CONSOLE_EDITORS));
            guiEditor = new EditorImpl("open", "--wait-apps", "--new", "-t");
        }

        @Override
        public Editor getConsoleEditor() {
            return consoleEditor;
        }

        @Override
        public Editor getGUIEditor() {
            return guiEditor;
        }
    }

    private static class DefaultEditorProvider implements EditorProviderDelegate {
        private final Editor consoleEditorHelper;
        private final Editor guiEditorHelper;

        public DefaultEditorProvider() {
            consoleEditorHelper = new EditorImpl(firstValidCommand(EDITOR, DEFAULT_EDITOR, CONSOLE_EDITORS));
            guiEditorHelper = new EditorImpl(firstValidCommand(VISUAL, DEFAULT_GUI_EDITOR, GUI_EDITORS));
        }

        @Override
        public Editor getConsoleEditor() {
            return consoleEditorHelper;
        }

        @Override
        public Editor getGUIEditor() {
            return guiEditorHelper;
        }
    }

    private static String firstValidCommand(String environment, String defaultCommand, String[] commands) {
        String editor = System.getenv(environment);
        if (editor != null && !editor.trim().isEmpty()) {
            return editor;
        }
        for (String command : commands) {
            if (command == null || command.isEmpty()) {
                continue;
            }
            File file = new File(command);
            if (file.exists() && !file.isDirectory()) {
                return command;
            }
        }
        return defaultCommand;
    }

    private static final class EditorImpl implements Editor {
        private final String[] command;

        public EditorImpl(String... command) {
            if (command.length == 0) {
                throw new IllegalArgumentException();
            }
            this.command = command;
        }

        @Override
        public boolean editFile(File file) {
            String[] cmd = new String[command.length + 1];
            System.arraycopy(command, 0, cmd, 0, command.length);
            cmd[cmd.length - 1] = file.getAbsolutePath();

            Process process;
            try {
                process = new ProcessBuilder(cmd).start();
            } catch (IOException e) {
                LOGGER.info("Error starting command: {}", Arrays.toString(cmd), e);
                return false;
            }

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeInterruptedException(e);
            }

            return true;
        }
    }
}
