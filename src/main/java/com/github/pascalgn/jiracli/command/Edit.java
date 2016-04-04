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
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = "edit", description = "Edit the given issues in a text editor")
class Edit implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Edit.class);

    private static final String CUSTOMFIELD_PREFIX = "customfield_";

    private static final String COMMENT = ";";
    private static final String NEWLINE = "\r\n";

    private static final Pattern ISSUE_TITLE = Pattern.compile("==\\s*([A-Z][A-Z0-9]*-[0-9]+)\\s*==\\s*");
    private static final Pattern FIELD_MULTILINE = Pattern.compile("([a-z][a-zA-Z0-9_]*)::\\s*");
    private static final Pattern FIELD_VALUE = Pattern.compile("([a-z][a-zA-Z0-9_]*):\\s+(.*)");

    private enum ReadState {
        EXPECT_ISSUE_TITLE, EXPECT_FIELD_ID, EXPECT_FIELD_CONTENT_MULTILINE;
    }

    @Argument(names = { "-p", "--print" }, description = "Print the text content instead of opening the editor")
    private boolean print;

    @Override
    public Data execute(Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        List<Issue> issues = issueList.remaining();

        try {
            File tempFile = File.createTempFile("edit-issues", ".txt");
            try {
                try (BufferedWriter writer = IOUtils.createBufferedWriter(tempFile)) {
                    for (Issue issue : issues) {
                        write(writer, issue);
                    }
                }

                if (print) {
                    return new Text(IOUtils.toString(tempFile));
                } else {
                    boolean success = context.getConsole().editFile(tempFile);
                    if (success) {
                        List<Issue> result;
                        try (BufferedReader reader = IOUtils.createBufferedReader(tempFile)) {
                            result = read(issues, reader);
                        }
                        return new IssueList(result.iterator());
                    } else {
                        return new IssueList(issues.iterator());
                    }
                }
            } finally {
                if (!tempFile.delete() && tempFile.exists()) {
                    LOGGER.warn("Could not delete temporary file: {}", tempFile);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void write(BufferedWriter writer, Issue issue) throws IOException {
        writer.write("== ");
        writer.write(issue.getKey());
        writer.write(" ==");
        writer.newLine();

        Collection<Field> editableFields = issue.getFieldMap().getEditableFields();
        if (editableFields.isEmpty()) {
            writer.write(COMMENT + " no editable fields for this issue!");
            writer.newLine();
            writer.newLine();
        } else {
            writer.newLine();
            writeFields(writer, editableFields);
        }
    }

    private static void writeFields(BufferedWriter writer, Collection<Field> fields) throws IOException {
        for (Field field : fields) {
            String id = field.getId();

            if (id.startsWith(CUSTOMFIELD_PREFIX)) {
                writer.write(COMMENT + " " + field.getName());
                writer.newLine();
            }

            writer.write(id);

            Object val = field.getValue().getValue();
            if (val == JSONObject.NULL) {
                val = null;
            }

            String str = Objects.toString(val, "");
            if (str.contains("\r") || str.contains("\n")) {
                writer.write("::");
                writer.newLine();
                try (BufferedReader reader = new BufferedReader(new StringReader(str))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.write(line);
                        writer.newLine();
                    }
                }
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

    static List<Issue> read(List<Issue> originalIssues, BufferedReader reader) throws IOException {
        ReadState s = ReadState.EXPECT_ISSUE_TITLE;

        Issue issue = null;
        Field field = null;
        StringBuilder content = null;

        String line;
        while ((line = reader.readLine()) != null) {
            switch (s) {
            case EXPECT_ISSUE_TITLE:
                if (commentOrEmpty(line)) {
                    continue;
                } else {
                    Matcher m = ISSUE_TITLE.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1);
                        issue = findIssue(originalIssues, key);
                        s = ReadState.EXPECT_FIELD_ID;
                        continue;
                    }
                }
                break;

            case EXPECT_FIELD_ID:
                if (commentOrEmpty(line)) {
                    continue;
                } else {
                    Matcher mm = FIELD_MULTILINE.matcher(line);
                    if (mm.matches()) {
                        String id = mm.group(1);
                        field = getField(issue, id);
                        s = ReadState.EXPECT_FIELD_CONTENT_MULTILINE;
                        continue;
                    }

                    Matcher mv = FIELD_VALUE.matcher(line);
                    if (mv.matches()) {
                        String id = mv.group(1);
                        String value = mv.group(2);
                        Field f = getField(issue, id);
                        setFieldValue(f, value);
                        continue;
                    }
                }
                break;

            case EXPECT_FIELD_CONTENT_MULTILINE:
                if (line.equals(".")) {
                    setFieldValue(field, content == null ? "" : content.toString());
                    field = null;
                    content = null;
                    s = ReadState.EXPECT_FIELD_ID;
                } else {
                    String str = (line.startsWith(".") ? line.substring(1) : line);
                    if (content == null) {
                        content = new StringBuilder(str);
                    } else {
                        content.append(NEWLINE);
                        content.append(str);
                    }
                }
                continue;

            default:
                break;
            }

            throw new IllegalStateException("Unexpected line: " + line);
        }

        return originalIssues;
    }

    private static Issue findIssue(List<Issue> issues, String key) {
        for (Issue issue : issues) {
            if (issue.getKey().equals(key)) {
                return issue;
            }
        }
        throw new IllegalStateException("No such issue: " + key);
    }

    private static boolean commentOrEmpty(String line) {
        return line.startsWith(COMMENT) || line.trim().isEmpty();
    }

    private static Field getField(Issue issue, String id) {
        Field field = issue.getFieldMap().getFieldById(id);
        if (field == null) {
            throw new IllegalStateException("No such field for issue " + issue + ": " + id);
        }
        return field;
    }

    private static void setFieldValue(Field field, String str) {
        Value value = field.getValue();
        String original = Objects.toString(value.getValue(), "");
        if (!str.equals(original)) {
            value.setValue(str);
        }
    }
}
