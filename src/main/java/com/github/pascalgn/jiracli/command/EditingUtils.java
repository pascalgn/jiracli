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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.context.WebService.CreateRequest;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Schema;

class EditingUtils {
    private static final String NO_EDITABLE_FIELDS = "no editable fields for this issue!";

    private static final String CREATE_ISSUE = "Create issue";
    private static final String NO_REQUIRED_FIELDS = "no required fields!";

    private static final String CUSTOMFIELD_PREFIX = "customfield_";

    private static final String COMMENT = ";";
    private static final String NEWLINE = "\r\n";

    private static final Pattern CREATE_ISSUE_TITLE = Pattern.compile("==\\s*(create\\s+issue)\\s*==\\s*",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ISSUE_TITLE = Pattern.compile("==\\s*([A-Z][A-Z0-9]*-[0-9]+)\\s*==\\s*");
    private static final Pattern FIELD_MULTILINE = Pattern.compile("([a-z][a-zA-Z0-9_]*)::\\s*");
    private static final Pattern FIELD_VALUE = Pattern.compile("([a-z][a-zA-Z0-9_]*):\\s+(.*)");

    private static final Pattern SORT_ISSUE = Pattern.compile("([A-Z][A-Z0-9]*-[0-9]+)($|\\s+.*)");

    private enum ReadState {
        EXPECT_ISSUE_TITLE, EXPECT_FIELD_OR_ISSUE, EXPECT_FIELD_CONTENT_MULTILINE;
    }

    public static void writeEdit(BufferedWriter writer, Issue issue, Collection<Field> fields, Schema schema)
            throws IOException {
        write(writer, issue.getKey(), toEditingFields(fields, schema), NO_EDITABLE_FIELDS);
    }

    private static Collection<EditingField> toEditingFields(Collection<Field> fields, Schema schema) {
        Collection<EditingField> editingFields = new ArrayList<EditingField>(fields.size());
        for (Field field : fields) {
            Object value = field.getValue().get();
            Converter converter = schema.getConverter(field.getId());
            String str = converter.toString(value);
            String fieldName = schema.getName(field.getId());
            editingFields.add(new EditingField(field.getId(), fieldName, str));
        }
        return editingFields;
    }

    public static void writeCreate(BufferedWriter writer, Collection<EditingField> fields) throws IOException {
        write(writer, CREATE_ISSUE, fields, NO_REQUIRED_FIELDS);
    }

    private static void write(BufferedWriter writer, String title, Collection<EditingField> fields, String noFields)
            throws IOException {
        writer.write("== ");
        writer.write(title);
        writer.write(" ==");
        writer.newLine();

        if (fields.isEmpty()) {
            writer.write(COMMENT + " " + noFields);
            writer.newLine();
            writer.newLine();
        } else {
            writer.newLine();
            writeFields(writer, fields);
        }
    }

    private static void writeFields(BufferedWriter writer, Collection<EditingField> fields) throws IOException {
        for (EditingField field : fields) {
            String id = field.getId();

            if (id.startsWith(CUSTOMFIELD_PREFIX)) {
                writer.write(COMMENT + " " + field.getName());
                writer.newLine();
            }

            writer.write(id);

            String value = field.getValue();
            if (value.contains("\r") || value.contains("\n")) {
                writer.write("::");
                writer.newLine();
                try (BufferedReader reader = new BufferedReader(new StringReader(value))) {
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
                writer.write(value);
                writer.newLine();
            }
            writer.newLine();
        }
        writer.newLine();
    }

    public static void writeSort(BufferedWriter writer, Collection<Issue> issues, Schema schema, String format)
            throws IOException {
        for (Issue issue : issues) {
            String s = CommandUtils.toString(issue, schema, format, null);
            writer.write(issue.getKey() + (s.isEmpty() ? "" : " " + s));
            writer.newLine();
        }
    }

    public static List<Issue> readEdit(List<Issue> originalIssues, Schema schema, BufferedReader reader)
            throws IOException {
        List<IssueData> issueData = read(reader, ISSUE_TITLE);
        List<Issue> issues = new ArrayList<Issue>(issueData.size());
        for (IssueData data : issueData) {
            Issue issue = findIssue(originalIssues, data.getKey());
            for (Map.Entry<String, String> entry : data.getFields().entrySet()) {
                Field field = getField(issue, entry.getKey());
                setFieldValue(field, entry.getValue(), schema);
            }
            issues.add(issue);
        }
        return issues;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<CreateRequest> readCreate(BufferedReader reader) throws IOException {
        return (List) read(reader, CREATE_ISSUE_TITLE);
    }

    private static List<IssueData> read(BufferedReader reader, Pattern issueTitle) throws IOException {
        List<IssueData> issues = new ArrayList<>();

        ReadState s = ReadState.EXPECT_ISSUE_TITLE;

        IssueData issue = null;
        String field = null;
        StringBuilder content = null;

        String line;
        while ((line = reader.readLine()) != null) {
            switch (s) {
            case EXPECT_ISSUE_TITLE:
                if (commentOrEmpty(line)) {
                    continue;
                } else {
                    Matcher m = issueTitle.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1);
                        issue = new IssueData(key);
                        issues.add(issue);
                        s = ReadState.EXPECT_FIELD_OR_ISSUE;
                        continue;
                    }
                }
                break;

            case EXPECT_FIELD_OR_ISSUE:
                if (commentOrEmpty(line)) {
                    continue;
                } else {
                    Matcher mi = issueTitle.matcher(line);
                    if (mi.matches()) {
                        String key = mi.group(1);
                        issue = new IssueData(key);
                        issues.add(issue);
                        s = ReadState.EXPECT_FIELD_OR_ISSUE;
                        continue;
                    }

                    Matcher mm = FIELD_MULTILINE.matcher(line);
                    if (mm.matches()) {
                        field = mm.group(1);
                        s = ReadState.EXPECT_FIELD_CONTENT_MULTILINE;
                        continue;
                    }

                    Matcher mv = FIELD_VALUE.matcher(line);
                    if (mv.matches()) {
                        String id = mv.group(1);
                        String value = Objects.toString(mv.group(2), "").trim();
                        issue.setField(id, value);
                        continue;
                    }
                }
                break;

            case EXPECT_FIELD_CONTENT_MULTILINE:
                if (line.equals(".")) {
                    issue.setField(field, content == null ? "" : content.toString().trim());
                    field = null;
                    content = null;
                    s = ReadState.EXPECT_FIELD_OR_ISSUE;
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

            throw new IllegalArgumentException("Unexpected line: " + line);
        }

        return issues;
    }

    public static List<Issue> readSort(List<Issue> originalIssues, BufferedReader reader) throws IOException {
        List<Issue> issues = new ArrayList<Issue>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (commentOrEmpty(line)) {
                continue;
            }
            Matcher m = SORT_ISSUE.matcher(line);
            if (m.matches()) {
                String key = m.group(1);
                issues.add(findIssue(originalIssues, key));
            } else {
                throw new IllegalArgumentException("Unexpected line: " + line);
            }
        }

        return issues;
    }

    private static Issue findIssue(List<Issue> issues, String key) {
        for (Issue issue : issues) {
            if (issue.getKey().equals(key)) {
                return issue;
            }
        }
        throw new IllegalArgumentException("No such issue: " + key);
    }

    private static boolean commentOrEmpty(String line) {
        return line.startsWith(COMMENT) || line.trim().isEmpty();
    }

    private static Field getField(Issue issue, String id) {
        Field field = issue.getFieldMap().getFieldById(id);
        if (field == null) {
            throw new IllegalArgumentException("No such field for issue " + issue + ": " + id);
        }
        return field;
    }

    private static void setFieldValue(Field field, String str, Schema schema) {
        Object value = field.getValue().get();
        Converter converter = schema.getConverter(field.getId());
        String original = converter.toString(value);
        if (!str.equals(original) && (original == null || !str.equals(original.trim()))) {
            Object newValue = converter.fromString(str);
            field.getValue().set(newValue);
        }
    }

    private static class IssueData implements CreateRequest {
        private final String key;
        private final Map<String, String> fields;

        public IssueData(String key) {
            this.key = key;
            this.fields = new LinkedHashMap<String, String>();
        }

        public String getKey() {
            return key;
        }

        @Override
        public Map<String, String> getFields() {
            return fields;
        }

        public void setField(String id, String value) {
            fields.put(id, value);
        }
    }

    public static class EditingField {
        private final String id;
        private final String name;
        private final String value;

        public EditingField(String id, String name, String value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
