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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.ReflectionUtils;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "get", description = "Return the field value for the given field")
class Get implements Command {
    @Argument(names = { "-r", "--raw" }, description = "get the raw field value")
    private boolean raw;

    @Argument(parameters = Parameters.ONE_OR_MORE, variable = "<field>", description = "the fields to output")
    private List<String> fields;

    @Override
    public TextList execute(final Context context, Data input) {
        fields = CommandUtils.getFields(fields);

        IssueList issueList = input.toIssueList();
        if (issueList == null) {
            final Iterator<Data> iterator = input.toIterator(Hint.none());
            return new TextList(new Supplier<Text>() {
                @Override
                public Text get(Set<Hint> hints) {
                    if (iterator.hasNext()) {
                        Data data = iterator.next();
                        StringBuilder str = new StringBuilder();
                        boolean first = true;
                        for (String field : fields) {
                            if (first) {
                                first = false;
                            } else {
                                str.append("\t");
                            }
                            Object value = ReflectionUtils.getValue(data, field);
                            str.append(Objects.toString(value, ""));
                        }
                        return new Text(str.toString());
                    } else {
                        return null;
                    }
                }
            });
        } else {
            final Schema schema = context.getWebService().getSchema();
            return new TextList(issueList.convertingSupplier(IssueHint.fields(fields), new Function<Issue, Text>() {
                @Override
                public Text apply(Issue issue, Set<Hint> hints) {
                    StringBuilder str = new StringBuilder();
                    boolean first = true;
                    for (String field : fields) {
                        if (first) {
                            first = false;
                        } else {
                            str.append("\t");
                        }
                        if (field.equals("key")) {
                            str.append(issue.getKey());
                        } else {
                            if (raw) {
                                FieldMap fieldMap = issue.getFieldMap();
                                Field f = fieldMap.getField(field, schema);
                                if (f == null) {
                                    throw new IllegalArgumentException("Unknown field: " + field);
                                }
                                Object val = f.getValue().get();
                                str.append(Objects.toString(val, ""));
                            } else {
                                str.append(new FormatHelper(schema).getValue(issue, field));
                            }
                        }
                    }
                    return new Text(str.toString());
                }
            }));
        }
    }
}
