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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = "edit", description = "Edit the given issues in a text editor")
class Edit implements Command {
    @Argument(names = { "-p", "--print" }, description = "print the text content instead of opening the editor")
    private boolean print;

    @Argument(names = { "-f", "--fields" }, parameters = Parameters.ONE_OR_MORE, variable = "<field>",
            description = "the fields to edit")
    private List<String> fields;

    @Argument(names = { "-a", "--all" }, description = "show all fields, not only loaded fields")
    private boolean all;

    @Argument(names = { "-e", "--editable" }, description = "only show editable fields")
    private boolean editable;

    @Override
    public Data execute(final Context context, final Data input) {
        fields = CommandUtils.getFields(fields);

        if (fields == null) {
            if (all && editable) {
                throw new IllegalArgumentException("Cannot combine --all and --editable");
            }
        } else {
            if (all) {
                throw new IllegalArgumentException("Cannot combine --fields and --all");
            } else if (editable) {
                throw new IllegalArgumentException("Cannot combine --fields and --editable");
            }
        }

        return CommandUtils.withTemporaryFile("edit", ".txt", new Function<File, Data>() {
            @Override
            public Data apply(File tempFile, Set<Hint> hints) {
                IssueList issueList = input.toIssueList();
                if (issueList == null) {
                    Text text = input.toTextOrFail();
                    try {
                        return editText(context, tempFile, text);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    try {
                        return editIssues(context, tempFile, issueList);
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        });
    }

    private Data editText(Context context, File file, Text text) throws IOException {
        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            writer.write(text.getText());
        }

        boolean success = context.getConsole().editFile(file);
        if (success) {
            return new Text(IOUtils.toString(file));
        } else {
            return text;
        }
    }

    private Data editIssues(Context context, File file, IssueList issueList) throws IOException {
        Set<Hint> hints = new HashSet<Hint>();
        if (all) {
            hints.add(IssueHint.allFields());
        } else if (editable) {
            hints.add(IssueHint.editableFields());
        } else if (fields != null) {
            hints.addAll(IssueHint.fields(fields));
        }

        List<Issue> issues = issueList.remaining(hints);

        Schema schema = context.getWebService().getSchema();

        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            for (Issue issue : issues) {
                Collection<Field> fields;
                if (all) {
                    fields = issue.getFieldMap().getFields();
                } else if (editable) {
                    fields = context.getWebService().getEditableFields(issue);
                } else if (this.fields != null) {
                    fields = new ArrayList<Field>();
                    for (String name : this.fields) {
                        fields.add(issue.getFieldMap().getField(name, schema));
                    }
                } else {
                    fields = issue.getFieldMap().getLoadedFields();
                }
                EditingUtils.writeEdit(writer, issue, fields, schema);
            }
        }

        if (print) {
            return new Text(IOUtils.toString(file));
        } else {
            boolean success = context.getConsole().editFile(file);
            if (success) {
                List<Issue> result;
                try (BufferedReader reader = IOUtils.createBufferedReader(file)) {
                    result = EditingUtils.readEdit(issues, schema, reader);
                }
                return new IssueList(result.iterator());
            } else {
                return new IssueList(issues.iterator());
            }
        }
    }
}
