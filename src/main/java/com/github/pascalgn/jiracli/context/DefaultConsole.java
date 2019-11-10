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
package com.github.pascalgn.jiracli.context;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.pascalgn.jiracli.util.Credentials;

public class DefaultConsole extends AbstractConsole {
    private final BufferedReader reader;

    public DefaultConsole(Configuration configuration) {
        super(configuration);
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void print(String str) {
        System.out.print(str);
        System.out.flush();
    }

    @Override
    public void println(String str) {
        System.out.println(str);
    }

    @Override
    public List<String> readLines() {
        println("End the input with a single .");
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = doReadLine()) != null) {
            if (line.equals(".")) {
                break;
            } else if (line.startsWith(".")) {
                lines.add(line.substring(1));
            } else {
                lines.add(line);
            }
        }
        return lines;
    }

    @Override
    public String readCommand() {
        return doReadLine();
    }

    @Override
    public String readLine() {
        return doReadLine();
    }

    private String doReadLine() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("Error reading line!", e);
        }
    }

    @Override
    protected String provideBaseUrl() {
        print("Base URL: ");
        return doReadLine();
    }

    @Override
    protected Credentials provideCredentials(String username, String url) {
        println("Please enter the credentials for " + url);

        String user = username;
        if (user == null) {
            print("Username: ");
            user = Objects.toString(doReadLine(), "").trim();
        } else {
            user = user.trim();
            println("Username: " + user);
        }

        print("Password: ");
        char[] password = readPassword();

        if (user.isEmpty() && password.length == 0) {
            return Credentials.getAnonymous();
        } else {
            return Credentials.create(user, password);
        }
    }

    private char[] readPassword() {
        java.io.Console console = System.console();
        if (console == null) {
            String str = doReadLine();
            return (str == null ? null : str.toCharArray());
        } else {
            return console.readPassword();
        }
    }

    @Override
    public boolean editFile(File file) {
        return editFile(file, false);
    }
}
