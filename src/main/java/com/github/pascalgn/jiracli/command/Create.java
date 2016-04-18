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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.command.EditingUtils.EditingField;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.WebService.CreateRequest;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.FieldDescription;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.IssueType;
import com.github.pascalgn.jiracli.model.Project;
import com.github.pascalgn.jiracli.model.ProjectList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = "create", description = "Create new issues")
class Create implements Command {
    @Argument(names = "-c", parameters = Parameters.ONE, variable = "<count>",
            description = "Number of issues to create")
    private int count = 1;

    @Argument(names = { "-p", "--project" }, parameters = Parameters.ONE, variable = "<project>",
            description = "The project for which to create issues")
    private String project;

    @Argument(names = { "-t", "--type" }, parameters = Parameters.ONE, variable = "<issuetype>",
            description = "The type of issues to create")
    private String issueType;

    @Argument(parameters = Parameters.ZERO_OR_MORE, variable = "<field-value>", description = "default field values")
    private List<String> fieldValues;

    @Override
    public Data execute(final Context context, Data input) {
        final Project project;
        if (this.project != null) {
            project = context.getWebService().getProject(this.project);
        } else {
            ProjectList projectList = input.toProjectList();
            if (projectList == null) {
                project = null;
            } else {
                project = projectList.next(Hint.none());
            }
        }

        final IssueType issueType;
        if (this.issueType != null) {
            if (project == null) {
                throw new IllegalArgumentException("Type given, but no project given!");
            } else {
                issueType = findIssueType(project.getIssueTypes(), this.issueType);
            }
        } else {
            issueType = null;
        }

        List<CreateRequest> createRequests = CommandUtils.withTemporaryFile("create", ".txt",
                new Function<File, List<CreateRequest>>() {
                    @Override
                    public List<CreateRequest> apply(File tempFile, Set<Hint> hints) {
                        try {
                            return getCreateRequests(context, project, issueType, tempFile);
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                });

        if (createRequests == null) {
            return new IssueList();
        } else {
            Iterator<CreateRequest> it = createRequests.iterator();
            while (it.hasNext()) {
                CreateRequest createRequest = it.next();
                if (createRequest.getFields().isEmpty()) {
                    it.remove();
                }
            }
            if (createRequests.isEmpty()) {
                return new IssueList();
            } else {
                List<Issue> issues = context.getWebService().createIssues(createRequests);
                return new IssueList(issues.iterator());
            }
        }
    }

    private static IssueType findIssueType(List<IssueType> issueTypes, String key) {
        Integer id;
        try {
            id = Integer.parseInt(key);
        } catch (NumberFormatException e) {
            id = null;
        }
        for (IssueType issueType : issueTypes) {
            if (id != null && issueType.getId() == id) {
                return issueType;
            }
            if (issueType.getName().equalsIgnoreCase(key)) {
                return issueType;
            }
        }
        throw new IllegalArgumentException("Unknown issue type: " + key);
    }

    private List<CreateRequest> getCreateRequests(Context context, Project project, IssueType issueType, File file)
            throws IOException {
        Collection<EditingField> fields = new ArrayList<EditingUtils.EditingField>();
        if (project != null) {
            fields.add(new EditingField("project", "Project", project.getKey()));
        }

        if (issueType != null) {
            fields.add(new EditingField("issuetype", "Issue type", issueType.getName()));
        }

        Map<String, String> fieldValues = parseFieldValues(context);

        if (project != null && issueType != null) {
            List<FieldDescription> descriptions = context.getWebService().getFields(project, issueType);
            for (FieldDescription field : descriptions) {
                String id = field.getId();
                if (field.isRequired() || fieldValues.containsKey(id)) {
                    if (id.equals("project") || id.equals("issuetype")) {
                        continue;
                    }
                    String value = Objects.toString(fieldValues.get(id), "");
                    fields.add(new EditingField(id, field.getName(), value));
                    fieldValues.remove(id);
                }
            }
        }

        for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
            String id = entry.getKey();
            fields.add(new EditingField(id, id, entry.getValue()));
        }

        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            for (int i = 0; i < count; i++) {
                EditingUtils.writeCreate(writer, fields);
            }
        }

        boolean success = context.getConsole().editFile(file);
        if (success) {
            try (BufferedReader reader = IOUtils.createBufferedReader(file)) {
                return EditingUtils.readCreate(reader);
            }
        } else {
            return null;
        }
    }

    private Map<String, String> parseFieldValues(Context context) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        if (fieldValues != null) {
            Schema schema = context.getWebService().getSchema();
            for (String field : fieldValues) {
                String[] str = field.split("=", 2);
                for (int i = 0; i < str.length; i++) {
                    str[i] = str[i].trim();
                }
                if (str.length != 2 || str[0].isEmpty()) {
                    throw new IllegalArgumentException("Invalid field value, expected field=value: " + field);
                }
                map.put(schema.getId(str[0]), str[1]);
            }
        }
        return map;
    }
}
