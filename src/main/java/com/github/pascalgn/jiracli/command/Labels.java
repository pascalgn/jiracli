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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.StringUtils;

@CommandDescription(names = "labels", description = "Add or remove labels of the given issues")
class Labels implements Command {
    @Argument(names = { "-a", "--add" },
            parameters = Parameters.ONE_OR_MORE, variable = "<label>", description = "the labels to add")
    private List<String> add;

    @Argument(names = { "-r", "--remove" },
            parameters = Parameters.ONE_OR_MORE, variable = "<label>", description = "the labels to remove")
    private List<String> remove;

    @Override
    public Data execute(Context context, Data input) {
        Set<Hint> hints = IssueHint.fields("labels");
        IssueList issueList = input.toIssueListOrFail();
        if (add == null && remove == null) {
            return new TextList(issueList.convertingSupplier(hints, new Function<Issue, Text>() {
                @Override
                public Text apply(Issue issue, Set<Hint> hints) {
                    Value value = getLabels(issue);
                    JSONArray arr = (JSONArray) value.get();
                    return new Text(StringUtils.join(arr, ", "));
                }
            }));
        } else {
            final Collection<String> add = readLabels(this.add);
            final Collection<String> remove = readLabels(this.remove);

            return new IssueList(issueList.convertingSupplier(hints, new Function<Issue, Issue>() {
                @Override
                public Issue apply(Issue issue, Set<Hint> hints) {
                    if (!add.isEmpty()) {
                        Value value = getLabels(issue);
                        JSONArray arr = (JSONArray) value.get();
                        for (String label : add) {
                            if (!contains(arr, label)) {
                                arr.put(label);
                            }
                        }
                        value.set(arr);
                    }

                    if (!remove.isEmpty()) {
                        Value value = getLabels(issue);
                        JSONArray arr = (JSONArray) value.get();
                        Iterator<Object> it = arr.iterator();
                        while (it.hasNext()) {
                            Object obj = it.next();
                            if (remove.contains(obj)) {
                                it.remove();
                            }
                        }
                        value.set(arr);
                    }

                    return issue;
                }
            }));
        }
    }

    private static Value getLabels(Issue issue) {
        return issue.getFieldMap().getFieldById("labels").getValue();
    }

    private static boolean contains(JSONArray arr, String str) {
        for (Object obj : arr) {
            if (obj instanceof String && obj.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private static Collection<String> readLabels(List<String> labels) {
        if (labels == null || labels.isEmpty()) {
            return Collections.emptyList();
        } else {
            Collection<String> result = new LinkedHashSet<String>();
            for (String label : labels) {
                if (label.contains(",")) {
                    String[] strs = label.split(",");
                    for (String str : strs) {
                        String trimmed = str.trim();
                        if (!trimmed.isEmpty()) {
                            result.add(trimmed);
                        }
                    }
                } else {
                    String trimmed = label.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
            return result;
        }
    }
}
