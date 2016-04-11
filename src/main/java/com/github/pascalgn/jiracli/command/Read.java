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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.CollectingSupplier;
import com.github.pascalgn.jiracli.util.ExcelHelper;
import com.github.pascalgn.jiracli.util.ExcelHelper.CellHandler;
import com.github.pascalgn.jiracli.util.ExcelHelperFactory;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "read", "r" }, description = "Read issue keys from standard input")
class Read implements Command {
    private static final String STDIN_FILENAME = "-";

    @Argument(parameters = Parameters.ONE, variable = "<file>", description = "the file to read")
    private String filename = STDIN_FILENAME;

    @Argument(names = { "-s", "--sheet" }, parameters = Parameters.ONE, variable = "<sheet>",
            description = "the sheet to read, only used when reading Excel files")
    private String sheet;

    @Argument(names = { "-c", "--col" }, parameters = Parameters.ONE, variable = "<col>",
            description = "the column to read, only used when reading Excel files")
    private String column;

    public Read() {
        // default constructor
    }

    Read(String filename, String sheet, String column) {
        this.filename = filename;
        this.sheet = sheet;
        this.column = column;
    }

    @Override
    public IssueList execute(final Context context, Data input) {
        final Supplier<String> supplier;
        if (filename.equals(STDIN_FILENAME)) {
            final TextList textList = input.toTextList();
            if (textList == null) {
                final List<String> lines = context.getConsole().readLines();
                supplier = new CollectingSupplier<String>() {
                    @Override
                    protected Collection<String> nextItems() {
                        return (lines.isEmpty() ? null : CommandUtils.findIssues(lines.remove(0)));
                    }
                };
            } else {
                supplier = new CollectingSupplier<String>() {
                    @Override
                    protected Collection<String> nextItems() {
                        Text text = textList.next();
                        return (text == null ? null : CommandUtils.findIssues(text.getText()));
                    }
                };
            }
        } else {
            File file = new File(filename);
            if (filename.toLowerCase().endsWith(".xlsx")) {
                supplier = new ExcelReader(file, sheet, column);
            } else {
                supplier = new TextFileReader(file);
            }
        }
        return new IssueList(new CollectingSupplier<Issue>() {
            private static final int ISSUE_FETCH_SIZE = 10;

            @Override
            protected Collection<Issue> nextItems() {
                // Fetch multiple issues at once to reduce server requests
                List<String> keys = new ArrayList<String>();
                String key;
                while ((key = supplier.get()) != null) {
                    keys.add(key);
                    if (keys.size() >= ISSUE_FETCH_SIZE) {
                        break;
                    }
                }
                return (keys.isEmpty() ? null : context.getWebService().getIssues(keys));
            }
        });
    }

    static class TextFileReader implements Supplier<String> {
        private static final int READ_SIZE = 8 * 1024;

        private final File file;
        private long offset;

        private final Deque<String> keys;

        public TextFileReader(File file) {
            this.file = file;
            this.keys = new ArrayDeque<>();
        }

        @Override
        public String get() {
            if (keys.isEmpty()) {
                try {
                    readNext();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            return (keys.isEmpty() ? null : keys.removeFirst());
        }

        private void readNext() throws IOException {
            if (offset != -1) {
                try (RandomAccessFile f = new RandomAccessFile(file, "r")) {
                    f.seek(offset);
                    while (true) {
                        String line = f.readLine();
                        if (line == null) {
                            offset = -1;
                            break;
                        } else {
                            keys.addAll(CommandUtils.findIssues(line));
                            if (f.getFilePointer() - offset > READ_SIZE) {
                                offset = f.getFilePointer();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static class ExcelReader implements Supplier<String> {
        private final File file;
        private final String sheet;
        private final String column;

        private transient List<String> keys;
        private transient int index;

        public ExcelReader(File file, String sheet, String column) {
            this.file = file;
            this.sheet = sheet;
            this.column = column;
        }

        @Override
        public String get() {
            init();
            if (index < keys.size()) {
                return keys.get(index++);
            } else {
                return null;
            }
        }

        private synchronized void init() {
            if (keys == null) {
                keys = new ArrayList<String>();
                ExcelHelper excelHelper = ExcelHelperFactory.createExcelHelper();
                try (InputStream input = new FileInputStream(file)) {
                    CellHandler cellHandler = new CellHandler() {
                        @Override
                        public void handleCell(int row, String column, String value) {
                            if (ExcelReader.this.column != null) {
                                if (!ExcelReader.this.column.equals(column)) {
                                    return;
                                }
                            }
                            for (String key : CommandUtils.findIssues(value)) {
                                keys.add(key);
                            }
                        }
                    };
                    if (sheet == null) {
                        excelHelper.parseWorkbook(input, cellHandler);
                    } else {
                        excelHelper.parseWorkbook(input, Collections.singletonList(sheet), cellHandler);
                    }
                } catch (IOException e) {
                    throw new IllegalStateException("Error reading from file: " + file, e);
                }
            }
        }
    }
}
