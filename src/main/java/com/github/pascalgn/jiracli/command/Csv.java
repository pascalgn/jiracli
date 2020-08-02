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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.AttachmentList;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.BoardList;
import com.github.pascalgn.jiracli.model.CommentList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.FieldList;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.ListVisitor;
import com.github.pascalgn.jiracli.model.ProjectList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.SprintList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "csv", description = "Convert the input to CSV")
class Csv implements Command {
    private static final String CONTENT_TYPE = "text/csv";

    @Argument(parameters = Parameters.ONE_OR_MORE, variable = "<field>", description = "the fields to output")
    private List<String> fields;

    @Argument(names = { "-s", "--sep" }, parameters = Parameters.ONE, variable = "<separator>",
            description = "the separator to use")
    private String separator = ";";

    @Argument(names = { "-h", "--header" }, description = "output a header row")
    private boolean header = true;

    @Override
    public TextList execute(final Context context, Data input) {
        fields = CommandUtils.getFields(fields);

        TextList rows = input.toListOrFail().accept(new ListVisitor<TextList>() {
            @Override
            public TextList visit(AttachmentList list) {
                return null;
            }

            @Override
            public TextList visit(BoardList list) {
                return toCsv(list.convertingSupplier(new Function<Board, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(Board board, Set<Hint> hints) {
                        return board.toMap();
                    }
                }));
            }

            @Override
            public TextList visit(CommentList list) {
                return null;
            }

            @Override
            public TextList visit(FieldList list) {
                return null;
            }

            @Override
            public TextList visit(IssueList list) {
                return toCsv(context, list);
            }

            @Override
            public TextList visit(ProjectList list) {
                return null;
            }

            @Override
            public TextList visit(SprintList list) {
                return toCsv(list.convertingSupplier(new Function<Sprint, Map<String, Object>>() {
                    @Override
                    public Map<String, Object> apply(Sprint sprint, Set<Hint> hints) {
                        return sprint.toMap();
                    }
                }));
            }

            @Override
            public TextList visit(TextList list) {
                return toCsv(list);
            }
        });

        if (rows == null) {
            throw new IllegalStateException("Invalid input type: " + input.getClass().getSimpleName());
        }

        if (header) {
            TextList head = new TextList(new Text(StringUtils.join(fields, separator)));
            return new TextList(CONTENT_TYPE, head, rows);
        } else {
            return rows;
        }
    }

    private TextList toCsv(Context context, IssueList issueList) {
        final Schema schema = context.getWebService().getSchema();
        Set<Hint> hints = IssueHint.fields(fields);
        return new TextList(CONTENT_TYPE, issueList.convertingSupplier(hints, new Function<Issue, Text>() {
            @Override
            public Text apply(Issue issue, Set<Hint> hints) {
                List<String> values = new ArrayList<>(fields.size());
                for (String field : fields) {
                    String value = new FormatHelper(schema).getValue(issue, field);
                    values.add(value);
                }
                return new Text(StringUtils.join(values, separator));
            }
        }));
    }

    private TextList toCsv(TextList textList) {
        final String empty = StringUtils.repeat(separator, fields.size() - 1);
        return new TextList(CONTENT_TYPE, textList.convertingSupplier(new Function<Text, Text>() {
            @Override
            public Text apply(Text text, Set<Hint> hints) {
                JSONObject json = JsonUtils.toJsonObject(text.getText());
                if (json == null) {
                    return new Text(empty);
                } else {
                    List<String> values = new ArrayList<>(fields.size());
                    for (String field : fields) {
                        values.add(json.optString(field, ""));
                    }
                    return new Text(StringUtils.join(values, separator));
                }
            }
        }));
    }

    private TextList toCsv(final Supplier<Map<String, Object>> supplier) {
        return new TextList(CONTENT_TYPE, new Supplier<Text>() {
            @Override
            public Text get(Set<Hint> hints) {
                Map<String, Object> next = supplier.get(hints);
                if (next == null) {
                    return null;
                } else {
                    List<String> values = new ArrayList<>(fields.size());
                    for (String field : fields) {
                        values.add(Objects.toString(next.get(field), ""));
                    }
                    return new Text(StringUtils.join(values, separator));
                }
            }
        });
    }
}
