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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.ConversionUtils;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "json", description = "Format Json strings")
class Json implements Command {
    private static final String CONTENT_TYPE = "application/json";

    @Argument(names = "-i", parameters = Parameters.ONE, variable = "<indent>", description = "indentation")
    private int indent = 2;

    @Argument(names = { "-j", "--join" }, description = "join lists into a single array")
    private boolean join;

    @Argument(names = { "-f", "--fields" }, parameters = Parameters.ONE_OR_MORE, variable = "<field>",
            description = "the fields to include")
    private List<String> fields;

    @Override
    public Data execute(Context context, Data input) {
        fields = CommandUtils.getFields(fields);

        IssueList issueList = input.toIssueList();
        if (issueList == null) {
            if (fields != null) {
                throw new IllegalArgumentException("Fields may only be given for issue lists!");
            }
            TextList textList = input.toTextListOrFail();
            if (join) {
                List<Text> texts = textList.remaining(Hint.none());
                if (texts.isEmpty()) {
                    return new Text("");
                } else if (texts.size() == 1) {
                    Text text = texts.get(0);
                    Object parsed = parse(text.getText());
                    return new Text(CONTENT_TYPE, format(parsed));
                } else {
                    JSONArray arr = new JSONArray();
                    for (Text text : texts) {
                        Object parsed = parse(text.getText());
                        arr.put(parsed);
                    }
                    return new Text(CONTENT_TYPE, format(arr));
                }
            } else {
                return new TextList(CONTENT_TYPE, textList.convertingSupplier(new Function<Text, Text>() {
                    @Override
                    public Text apply(Text text, Set<Hint> hints) {
                        Object parsed = parse(text.getText());
                        return new Text(CONTENT_TYPE, format(parsed));
                    }
                }));
            }
        } else {
            Set<Hint> hints = IssueHint.fields(fields);
            if (join) {
                List<Issue> issues = issueList.remaining(hints);
                JSONArray arr = new JSONArray();
                for (Issue issue : issues) {
                    arr.put(ConversionUtils.toJson(issue, fields));
                }
                return new Text(CONTENT_TYPE, format(arr));
            } else {
                return new TextList(CONTENT_TYPE, issueList.convertingSupplier(hints, new Function<Issue, Text>() {
                    @Override
                    public Text apply(Issue issue, Set<Hint> hints) {
                        String json = ConversionUtils.toJson(issue, fields).toString(indent);
                        return new Text(CONTENT_TYPE, json);
                    }
                }));
            }
        }
    }

    private String format(Object obj) {
        if (indent > 0) {
            if (obj instanceof JSONObject) {
                return ((JSONObject) obj).toString(indent);
            } else if (obj instanceof JSONArray) {
                return ((JSONArray) obj).toString(indent);
            }
        }
        return Objects.toString(obj, "");
    }

    private Object parse(String str) {
        JSONObject obj = JsonUtils.toJsonObject(str);
        if (obj == null) {
            JSONArray arr = JsonUtils.toJsonArray(str);
            if (arr == null) {
                throw new IllegalArgumentException("Invalid Json: " + str);
            } else {
                return arr;
            }
        } else {
            return obj;
        }
    }
}
