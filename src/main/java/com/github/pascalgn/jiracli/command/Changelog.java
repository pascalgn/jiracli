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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Change;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.StringUtils;

import difflib.DiffUtils;
import difflib.Patch;

@CommandDescription(names = "changelog", description = "Show the changelog for the given issues")
class Changelog implements Command {
    @Argument(parameters = Parameters.ONE, variable = "<field>", description = "the field")
    private String field;

    @Argument(names = { "-c", "--context" }, parameters = Parameters.ONE, variable = "<context>",
            description = "Lines of context to show")
    private int contextLines = 0;

    @Override
    public Data execute(final Context context, Data input) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss");
        Set<Hint> hints = Collections.<Hint> singleton(IssueHint.changelog());
        return new TextList(input.toIssueListOrFail().loadingSupplier(hints, new Function<Issue, Collection<Text>>() {
            @Override
            public Collection<Text> apply(Issue issue, Set<Hint> hints) {
                List<Text> texts = new ArrayList<>();
                texts.add(new Text("### " + issue.getKey() + "  " + context.getWebService().getUrl(issue)));

                List<Change> changes = context.getWebService().getChanges(issue);
                String currentInfo = null;
                for (Change change : changes) {
                    for (Change.Item item : change.getItems()) {
                        if (item.getField().equals(field)) {
                            if (currentInfo == null) {
                                currentInfo = toInfoString(issue, dateFormat);
                            }
                            String originalInfo = currentInfo;
                            String revisedInfo = toInfoString(change, dateFormat);
                            List<String> original = StringUtils.splitNewline(item.getFrom());
                            List<String> revised = StringUtils.splitNewline(item.getTo());
                            Patch<String> patch = DiffUtils.diff(original, revised);
                            List<String> diff = DiffUtils.generateUnifiedDiff(originalInfo, revisedInfo, original,
                                    patch, contextLines);
                            texts.add(new Text(diff));
                            currentInfo = revisedInfo;
                        }
                    }
                }

                return texts;
            }
        }));
    }

    private static String toInfoString(Issue issue, DateFormat dateFormat) {
        DateFormat parse = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        FieldMap fieldMap = issue.getFieldMap();
        String created = Objects.toString(fieldMap.getFieldById("created").getValue().get(), "");
        Date date;
        try {
            date = parse.parse(created);
        } catch (ParseException e) {
            throw new IllegalStateException("Invalid date: " + created, e);
        }
        JSONObject creator = (JSONObject) fieldMap.getFieldById("creator").getValue().get();
        return creator.getString("displayName") + ", " + dateFormat.format(date);
    }

    private static String toInfoString(Change change, DateFormat dateFormat) {
        return change.getUser().getName() + ", " + dateFormat.format(change.getDate());
    }
}
