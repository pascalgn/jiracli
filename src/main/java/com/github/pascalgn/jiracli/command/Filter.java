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

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "filter", description = "Filter issues by the given field value")
class Filter implements Command {
    @Argument(parameters = Parameters.ONE, variable = "<field>", order = 1, description = "issue's field name")
    private String field;

    @Argument(parameters = Parameters.ONE, variable = "<value>", order = 2, description = "the filter value")
    private Pattern pattern;

    public Filter() {
        // default constructor
    }

    Filter(String field, String value) {
        this.field = field;
        this.pattern = Pattern.compile(value);
    }

    @Override
    public IssueList execute(final Context context, final Data input) {
        final IssueList issueList = input.toIssueList();
        return new IssueList(new Supplier<Issue>() {
            @Override
            public Issue get() {
                Issue issue;
                while ((issue = issueList.next()) != null) {
                    if (matches(context, issue)) {
                        break;
                    }
                }
                return issue;
            }
        });
    }

    boolean matches(Context context, Issue issue) {
        Object obj = CommandUtils.getFieldValue(issue, field);
        String fieldValue = Objects.toString(obj, "");
        return pattern.matcher(fieldValue).find();
    }
}
