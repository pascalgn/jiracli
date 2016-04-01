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

import java.io.File;
import java.io.IOException;

import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.Supplier;

public class DelegateConsole implements Console {
    private Consumer<String> appendText;
    private Supplier<String> readLine;

    public DelegateConsole(Consumer<String> appendText, Supplier<String> readLine) {
        this.appendText = appendText;
        this.readLine = readLine;
    }

    @Override
    public void print(String str) {
        appendText.accept(str);
    }

    @Override
    public void println(String str) {
        appendText.accept(str + "\n");
    }

    @Override
    public String readLine() {
        return readLine.get();
    }

    @Override
    public char[] readPassword() {
        String str = readLine();
        return (str == null ? null : str.toCharArray());
    }

    @Override
    public boolean editFile(File file) {
        Process process;
        try {
            process = new ProcessBuilder("C:/Windows/System32/notepad.exe", file.getAbsolutePath()).start();
        } catch (IOException e) {
            return false;
        }
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
