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

import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "filter", description = "Filter issues by the given field value")
class Filter implements Command {
    @Argument(names = { "-e", "--regexp" }, description = "Use regular expressions")
    private boolean regexp;

    @Argument(names = { "-i", "--ignore" }, description = "Ignore case")
    private boolean ignoreCase;

    @Argument(names = { "-f", "--field" }, parameters = Parameters.ONE, variable = "<field>",
            description = "issue's field name")
    private String field;

    @Argument(parameters = Parameters.ONE, variable = "<value>", order = 2, description = "the filter value")
    private String search;

    public Filter() {
        // default constructor
    }

    Filter(String field, String search) {
        this.field = field;
        this.search = search;
    }

    Filter(boolean regexp, boolean ignoreCase, String field, String search) {
        this.regexp = regexp;
        this.ignoreCase = ignoreCase;
        this.field = field;
        this.search = search;
    }

    @Override
    public Data execute(final Context context, final Data input) {
        int flags = 0;
        if (!regexp) {
            flags |= Pattern.LITERAL;
        }
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        final Pattern pattern = Pattern.compile(search, flags);
        if (field == null) {
            final TextList textList = input.toTextListOrFail();
            return new TextList(new Supplier<Text>() {
                @Override
                public Text get() {
                    Text text;
                    while ((text = textList.next()) != null) {
                        String str = text.getText();
                        if (pattern.matcher(str).find()) {
                            return text;
                        }
                    }
                    return null;
                }
            });
        } else {
            final IssueList issueList = input.toIssueListOrFail();
            return new IssueList(new Supplier<Issue>() {
                @Override
                public Issue get() {
                    Issue issue;
                    while ((issue = issueList.next()) != null) {
                        if (matches(context, issue, pattern)) {
                            return issue;
                        }
                    }
                    return null;
                }
            });
        }
    }

    private boolean matches(Context context, Issue issue, Pattern pattern) {
        Schema schema = context.getWebService().getSchema();
        Object obj = CommandUtils.getFieldValue(issue, schema, field, "");
        return pattern.matcher(obj.toString()).find();
    }
}
