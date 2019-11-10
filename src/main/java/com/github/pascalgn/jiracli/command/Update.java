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
package com.github.pascalgn.jiracli.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.StringUtils;

@CommandDescription(names = "update", description = "Update the given issues on the server")
class Update implements Command {
    @Argument(names = { "-n", "--dry" }, description = "only print modified fields")
    private boolean dry;

    @Argument(names = { "-c", "--check" },
            description = "check if the modified fields of the issue are editable by the current user before updating")
    private boolean check;

    @Argument(names = "--continue",
            description = "continue with the other issues if there was an error updating an issue")
    private boolean continueOnError;

    @Argument(names = { "-E", "--no-email" },
            description = "disable email notifications, requires project admin privileges")
    private boolean noEmail;

    @Override
    public Data execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        if (dry) {
            return new TextList(issueList.loadingSupplier(new Function<Issue, Collection<Text>>() {
                @Override
                public Collection<Text> apply(Issue issue, Set<Hint> hints) {
                    List<String> invalid;
                    if (check) {
                        invalid = getInvalidFields(context, issue);
                    } else {
                        invalid = Collections.emptyList();
                    }

                    List<String> str = new ArrayList<String>();
                    for (Field field : issue.getFieldMap().getLoadedFields()) {
                        Value value = field.getValue();
                        if (value.modified()) {
                            if (invalid.contains(field.getId())) {
                                str.add("Not editable: " + field.getId());
                            } else {
                                str.add(field.getId() + " = " + value.get());
                            }
                        }
                    }

                    if (str.isEmpty()) {
                        return Collections.emptyList();
                    } else {
                        return Collections.singleton(new Text(str));
                    }
                }
            }));
        } else {
            final boolean notifyUsers = !noEmail;
            return new IssueList(issueList.convertingSupplier(new Function<Issue, Issue>() {
                @Override
                public Issue apply(Issue issue, Set<Hint> hints) {
                    try {
                        if (check) {
                            checkFields(context, issue);
                        }
                        try {
                            context.getWebService().updateIssue(issue, notifyUsers);
                        } catch (RuntimeException e) {
                            String message = "Error updating issue: " + issue;
                            String errorMessage = e.getLocalizedMessage();
                            if (!errorMessage.isEmpty()) {
                                message += ": " + errorMessage;
                            }
                            throw new IllegalStateException(message, e);
                        }
                    } catch (RuntimeException e) {
                        if (continueOnError) {
                            context.getConsole().println(e.getLocalizedMessage());
                        } else {
                            throw e;
                        }
                    }
                    return issue;
                }
            }));
        }
    }

    private static void checkFields(Context context, Issue issue) {
        List<String> invalid = getInvalidFields(context, issue);
        if (!invalid.isEmpty()) {
            String str = StringUtils.join(invalid, ", ");
            throw new IllegalStateException("Fields are not editable: " + issue.getKey() + ": " + str);
        }
    }

    private static List<String> getInvalidFields(Context context, Issue issue) {
        List<String> invalid = null;
        Collection<Field> editable = null;
        for (Field field : issue.getFieldMap().getLoadedFields()) {
            if (field.getValue().modified()) {
                if (editable == null) {
                    editable = context.getWebService().getEditableFields(issue);
                }
                if (!editable.contains(field)) {
                    if (invalid == null) {
                        invalid = new ArrayList<String>();
                    }
                    invalid.add(field.getId());
                }
            }
        }
        return (invalid == null ? Collections.<String> emptyList() : invalid);
    }
}
