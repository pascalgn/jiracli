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
import java.util.Set;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;
import com.github.pascalgn.jiracli.util.StringUtils;

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
    public TextList execute(Context context, Data input) {
        fields = CommandUtils.getFields(fields);

        IssueList issueList = input.toIssueList();
        TextList rows;
        if (issueList == null) {
            TextList textList = input.toTextListOrFail();
            rows = toCsv(textList);
        } else {
            rows = toCsv(context, issueList);
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
}
