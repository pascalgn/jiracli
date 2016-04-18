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

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "set", description = "Set field values")
class Set implements Command {
    @Argument(order = 1, parameters = Parameters.ONE, variable = "<field>", description = "The field")
    private String field;

    @Argument(order = 2, parameters = Parameters.ONE, variable = "<value>", description = "The value to set")
    private String value;

    @Override
    public IssueList execute(Context context, Data input) {
        final java.util.Set<Hint> hints = IssueHint.fields(field);
        final Schema schema = context.getWebService().getSchema();
        return new IssueList(input.toIssueListOrFail().convertingSupplier(hints, new Function<Issue, Issue>() {
            @Override
            public Issue apply(Issue issue, java.util.Set<Hint> h) {
                Field f = issue.getFieldMap().getField(field, schema);
                if (f == null) {
                    throw new IllegalArgumentException("Unknown field: " + field + ": " + issue);
                }
                Converter converter = schema.getConverter(f.getId());
                Object val = converter.fromString(value);
                f.getValue().set(val);
                return issue;
            }
        }));
    }
}
