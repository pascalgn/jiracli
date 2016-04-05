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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "read", "r" }, description = "Read issue keys from standard input")
class Read implements Command {
    private static final String STDIN_FILENAME = "-";

    private final String filename;

    public Read() {
        this(STDIN_FILENAME);
    }

    public Read(String filename) {
        this.filename = filename;
    }

    @Override
    public IssueList execute(Context context, Data input) {
        return new IssueList(getSupplier(context));
    }

    private Supplier<Issue> getSupplier(Context context) {
        return new TextReader(context, filename);
    }

    private static class TextReader implements Supplier<Issue> {
        private final Context context;
        private final String filename;

        private final Deque<Issue> issues;

        private transient Supplier<String> stringSupplier;

        public TextReader(Context context, String filename) {
            this.context = context;
            this.filename = filename;
            this.issues = new ArrayDeque<Issue>();
        }

        @Override
        public Issue get() {
            if (issues.isEmpty()) {
                String line = getStringSupplier().get();
                if (line == null) {
                    return null;
                }
                for (String key : CommandUtils.findIssues(line)) {
                    issues.add(context.getWebService().getIssue(key));
                }
            }
            return (issues.isEmpty() ? null : issues.removeFirst());
        }

        private synchronized Supplier<String> getStringSupplier() {
            if (stringSupplier == null) {
                if (filename.equals(STDIN_FILENAME)) {
                    stringSupplier = new Supplier<String>() {
                        @Override
                        public String get() {
                            return context.getConsole().readLine();
                        }
                    };
                } else {
                    final BufferedReader bufferedReader;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                    } catch (FileNotFoundException e) {
                        throw new IllegalStateException("File not found: " + filename);
                    }
                    context.onClose(new Runnable() {
                        @Override
                        public void run() {
                            closeUnchecked(bufferedReader);
                        }
                    });
                    stringSupplier = new Supplier<String>() {
                        @Override
                        public String get() {
                            try {
                                return bufferedReader.readLine();
                            } catch (IOException e) {
                                throw new IllegalStateException("Error reading from " + filename, e);
                            }
                        }
                    };
                }
            }
            return stringSupplier;
        }
    }

    private static void closeUnchecked(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new IllegalStateException("Exception while trying to close: " + closeable, e);
        }
    }
}
