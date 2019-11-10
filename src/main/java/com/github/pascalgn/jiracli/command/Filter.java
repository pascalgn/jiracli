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

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "filter", "grep" }, description = "Filter issues by the given field value")
class Filter implements Command {
    @Argument(names = { "-e", "-E", "--regexp" }, description = "use regular expressions")
    private boolean regexp;

    @Argument(names = { "-i", "--ignore-case" }, description = "ignore case")
    private boolean ignoreCase;

    @Argument(names = { "-j", "--javascript" }, description = "interpret value as a JavaScript expression")
    private boolean javaScript;

    @Argument(names = { "-v", "--inverse" }, description = "only return items that don't match")
    private boolean inverse;

    @Argument(names = { "-f", "--field" }, parameters = Parameters.ONE, variable = "<field>",
            description = "issue's field name")
    private String field;

    @Argument(parameters = Parameters.ONE, variable = "<value>", description = "the filter value")
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
        if (javaScript) {
            return filterJavaScript(context, input);
        } else {
            return filterValue(context, input);
        }
    }

    private Data filterJavaScript(final Context context, final Data input) {
        if (regexp) {
            throw new IllegalArgumentException("Cannot combine --javascript and --regexp!");
        } else if (ignoreCase) {
            throw new IllegalArgumentException("Cannot combine --javascript and --ignore-case!");
        } else if (field != null) {
            throw new IllegalArgumentException("Cannot combine --javascript and --field!");
        }

        final String js = search.trim();

        final IssueList issueList = input.toIssueList();
        if (issueList == null) {
            final TextList textList = input.toTextListOrFail();
            return new TextList(new Supplier<Text>() {
                @Override
                public Text get(Set<Hint> hints) {
                    Text text;
                    while ((text = textList.next(hints)) != null) {
                        boolean result = context.getJavaScriptEngine().test(js, text);
                        if (result ^ inverse) {
                            return text;
                        }
                    }
                    return null;
                }
            });
        } else {
            List<String> fields = CommandUtils.findJavaScriptFields(js);
            final Set<Hint> hints = IssueHint.fields(fields);
            return new IssueList(new Supplier<Issue>() {
                @Override
                public Issue get(Set<Hint> localHints) {
                    Set<Hint> combined = Hint.combine(hints, localHints);
                    Issue issue;
                    while ((issue = issueList.next(combined)) != null) {
                        boolean result = context.getJavaScriptEngine().test(js, issue);
                        if (result ^ inverse) {
                            return issue;
                        }
                    }
                    return null;
                }
            });
        }
    }

    private Data filterValue(final Context context, final Data input) {
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
                public Text get(Set<Hint> hints) {
                    Text text;
                    while ((text = textList.next(hints)) != null) {
                        String str = text.getText();
                        boolean result = pattern.matcher(str).find();
                        if (result ^ inverse) {
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
                public Issue get(Set<Hint> hints) {
                    Set<Hint> combined = Hint.combine(hints, IssueHint.fields(field));
                    Issue issue;
                    while ((issue = issueList.next(combined)) != null) {
                        boolean result = matches(context, issue, pattern);
                        if (result ^ inverse) {
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
        String value = new FormatHelper(schema).getValue(issue, field);
        return pattern.matcher(value).find();
    }
}
