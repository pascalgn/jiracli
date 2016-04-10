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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.ExcelHelper;
import com.github.pascalgn.jiracli.util.ExcelHelper.CellHandler;
import com.github.pascalgn.jiracli.util.ExcelHelperFactory;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "read", "r" }, description = "Read issue keys from standard input")
class Read implements Command {
    private static final String STDIN_FILENAME = "-";

    @Argument(parameters = Parameters.ONE, variable = "<file>", description = "the file to read")
    private String filename = STDIN_FILENAME;

    @Argument(names = { "-c", "--col" }, parameters = Parameters.ONE, variable = "<col>",
            description = "the column to read, only used when reading Excel files")
    private String column;

    public Read() {
        // default constructor
    }

    Read(String filename, String column) {
        this.filename = filename;
        this.column = column;
    }

    @Override
    public IssueList execute(Context context, Data input) {
        return new IssueList(getSupplier(context));
    }

    private Supplier<Issue> getSupplier(Context context) {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new ExcelReader(context, filename, column);
        } else {
            return new TextReader(context, filename);
        }
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
                String line;
                Supplier<String> supplier = getStringSupplier();
                while ((line = supplier.get()) != null) {
                    for (String key : CommandUtils.findIssues(line)) {
                        issues.add(context.getWebService().getIssue(key));
                    }
                    if (!issues.isEmpty()) {
                        break;
                    }
                }
            }
            return (issues.isEmpty() ? null : issues.removeFirst());
        }

        private synchronized Supplier<String> getStringSupplier() {
            if (stringSupplier == null) {
                if (filename.equals(STDIN_FILENAME)) {
                    final List<String> lines = context.getConsole().readLines();
                    stringSupplier = new Supplier<String>() {
                        @Override
                        public String get() {
                            return (lines.isEmpty() ? null : lines.remove(0));
                        }
                    };
                } else {
                    final BufferedReader bufferedReader;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
                    } catch (FileNotFoundException e) {
                        throw new IllegalArgumentException("File not found: " + filename);
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

    private static class ExcelReader implements Supplier<Issue> {
        private final Context context;
        private final String filename;
        private final String column;

        private transient List<Issue> issues;
        private transient int index;

        public ExcelReader(Context context, String filename, String column) {
            this.context = context;
            this.filename = filename;
            this.column = column;
        }

        @Override
        public Issue get() {
            init();
            if (index < issues.size()) {
                return issues.get(index++);
            } else {
                return null;
            }
        }

        private synchronized void init() {
            if (issues == null) {
                issues = new ArrayList<Issue>();
                ExcelHelper excelHelper = ExcelHelperFactory.createExcelHelper();
                try (InputStream input = new FileInputStream(filename)) {
                    excelHelper.parseWorkbook(input, new CellHandler() {
                        @Override
                        public void handleCell(int row, String column, String value) {
                            if (ExcelReader.this.column != null) {
                                if (!ExcelReader.this.column.equals(column)) {
                                    return;
                                }
                            }
                            for (String key : CommandUtils.findIssues(value)) {
                                issues.add(context.getWebService().getIssue(key));
                            }
                        }
                    });
                } catch (IOException e) {
                    throw new IllegalStateException("Error reading from file: " + filename, e);
                }
            }
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
