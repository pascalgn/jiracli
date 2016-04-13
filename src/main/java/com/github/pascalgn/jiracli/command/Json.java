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

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "json", description = "Parse Json strings")
class Json implements Command {
    @Argument(names = "-i", parameters = Parameters.ONE, variable = "<indent>", description = "Indentation")
    private int indent = 2;

    @Override
    public Data execute(Context context, Data input) {
        TextList textList = input.toTextListOrFail();
        List<Text> texts = textList.remaining(Hint.none());
        if (texts.isEmpty()) {
            return new Text("");
        } else if (texts.size() == 1) {
            Text text = texts.get(0);
            Object parsed = parse(text.getText());
            return new Text(toString(parsed));
        } else {
            JSONArray arr = new JSONArray();
            for (Text text : texts) {
                Object parsed = parse(text.getText());
                arr.put(parsed);
            }
            return new Text(toString(arr));
        }
    }

    private String toString(Object obj) {
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
