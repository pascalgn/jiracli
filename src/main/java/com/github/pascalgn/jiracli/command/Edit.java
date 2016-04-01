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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "edit", description = "Edit the given issues in a text editor")
class Edit implements Command {
    @Argument(names = { "-p", "--print" }, description = "Print the text content instead of opening the editor")
    private boolean print;

    @Override
    public IssueList execute(Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        try {
            File tempFile = File.createTempFile("edit-issues", ".txt");
            try (BufferedWriter writer = createBufferedWriter(tempFile)) {
                Issue issue;
                while ((issue = issueList.next()) != null) {
                    write(writer, issue);
                }
            }
            context.getConsole().editFile(tempFile);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return new IssueList(new Supplier<Issue>() {
            @Override
            public Issue get() {
                return null;
            }
        });
    }

    private static BufferedWriter createBufferedWriter(File file) throws FileNotFoundException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
    }

    private void write(BufferedWriter writer, Issue issue) throws IOException {
        writer.write("== ");
        writer.write(issue.getKey());
        writer.write(" ==");
        writer.newLine();

        Collection<Field> editableFields = issue.getFieldMap().getEditableFields();
        if (editableFields.isEmpty()) {
            writer.write("# no editable fields for this issue!");
            writer.newLine();
            writer.newLine();
        } else {
            writer.newLine();
            writeFields(writer, editableFields);
        }
    }

    private static void writeFields(BufferedWriter writer, Collection<Field> fields) throws IOException {
        for (Field field : fields) {
            Object val = field.getValue().getValue();
            if (val == JSONObject.NULL) {
                continue;
            }

            String str = Objects.toString(val, "");
            if (str.isEmpty()) {
                continue;
            }

            writer.write(field.getName());
            if (str.contains("\n") || field.getName().contains(":")) {
                writer.write("::");
                writer.newLine();
                writer.write(str);
                writer.newLine();
                writer.write(".");
                writer.newLine();
            } else {
                writer.write(": ");
                writer.write(str);
                writer.newLine();
            }
            writer.newLine();
            writer.newLine();
        }
    }
}
