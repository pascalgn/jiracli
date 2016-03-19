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

import java.util.Objects;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.IssueListType;
import com.github.pascalgn.jiracli.util.Supplier;

class Filter implements Command<IssueListType, IssueList, IssueList> {
    private final String field;
    private final Pattern pattern;

    public Filter(String field, String value) {
        this.field = field;
        this.pattern = Pattern.compile(value);
    }

    @Override
    public IssueListType getInputType() {
        return IssueListType.getInstance();
    }

    @Override
    public IssueList execute(final Context context, final IssueList input) {
        return new IssueList(new Supplier<Issue>() {
            @Override
            public Issue get() {
                Issue issue;
                while ((issue = input.next()) != null) {
                    if (matches(context, issue)) {
                        break;
                    }
                }
                return issue;
            }
        });
    }

    boolean matches(Context context, Issue issue) {
        WebService webService = context.getWebService();
        JSONObject json = webService.getIssue(issue.getKey());
        Object obj = CommandUtils.getFieldValue(webService, json, field);
        String fieldValue = Objects.toString(obj, "");
        return pattern.matcher(fieldValue).find();
    }
}
