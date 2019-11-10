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

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "replace", description = "Replace field values")
class Replace implements Command {
    @Argument(names = { "-e", "--regexp" }, description = "Use regular expressions")
    private boolean regexp;

    @Argument(names = { "-i", "--ignore" }, description = "Ignore case")
    private boolean ignoreCase;

    @Argument(order = 1, parameters = Parameters.ONE, variable = "<field>", description = "The field to search")
    private String field;

    @Argument(order = 2, parameters = Parameters.ONE, variable = "<search>", description = "The search value")
    private String search;

    @Argument(order = 3, parameters = Parameters.ONE, variable = "<replace>", description = "The replacement value")
    private String replace;

    @Override
    public Data execute(final Context context, Data input) {
        int flags = 0;
        if (!regexp) {
            flags |= Pattern.LITERAL;
        }
        if (ignoreCase) {
            flags |= Pattern.CASE_INSENSITIVE;
        }
        final Pattern pattern = Pattern.compile(search, flags);
        IssueList issueList = input.toIssueListOrFail();
        return new IssueList(issueList.convertingSupplier(new Function<Issue, Issue>() {
            @Override
            public Issue apply(Issue issue, Set<Hint> hints) {
                FieldMap fieldMap = issue.getFieldMap();
                final Schema schema = context.getWebService().getSchema();
                Field field = fieldMap.getField(Replace.this.field, schema);
                if (field == null) {
                    throw new IllegalArgumentException("Field not found: " + issue + ": " + Replace.this.field);
                }
                Object value = field.getValue().get();
                Converter converter = schema.getConverter(field.getId());
                String original = converter.toString(value);
                Matcher m = pattern.matcher(original);
                String str = m.replaceAll(replace);
                if (!str.equals(original)) {
                    Object newValue = converter.fromString(str);
                    field.getValue().set(newValue);
                }
                return issue;
            }
        }));
    }
}
