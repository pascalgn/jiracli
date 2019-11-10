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
package com.github.pascalgn.jiracli.testutil;

import java.io.File;
import java.util.Collections;
import java.util.List;

import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.util.Credentials;

public class MockConsole implements Console {
    private final String baseUrl;
    private final StringBuilder output;

    public MockConsole(String baseUrl) {
        this.baseUrl = baseUrl;
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
    public List<String> readLines() {
        return Collections.emptyList();
    }

    @Override
    public String readCommand() {
        return null;
    }

    @Override
    public String readLine() {
        return null;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public Credentials getCredentials(String url) {
        return Credentials.getAnonymous();
    }

    @Override
    public boolean editFile(File file) {
        return true;
    }

    @Override
    public void openFile(File file) {
    }

    @Override
    public void onInterrupt(Runnable runnable) {
    }
}
