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
package com.github.pascalgn.jiracli.testutil;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.github.pascalgn.jiracli.context.Console;

public class MockConsole implements Console {
    private final List<String> input;
    private final StringBuilder output;

    private int index;

    public MockConsole(String... input) {
        this.input = Arrays.asList(input);
        this.output = new StringBuilder();
    }

    public String getOutput() {
        return output.toString();
    }

    @Override
    public void print(String str) {
        output.append(str);
    }

    @Override
    public void println(String str) {
        output.append(str);
        output.append(System.lineSeparator());
    }

    @Override
    public String readLine() {
        return (index < input.size() ? input.get(index++) : null);
    }

    @Override
    public char[] readPassword() {
        String str = readLine();
        return (str == null ? null : str.toCharArray());
    }

    @Override
    public boolean editFile(File file) {
        return false;
    }
}
