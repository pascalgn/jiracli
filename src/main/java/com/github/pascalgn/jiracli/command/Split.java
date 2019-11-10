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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.json.JSONArray;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "split", description = "Split the input texts into multiple texts")
class Split implements Command {
    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<delimiter>",
            description = "the delimiter to use for splitting")
    private String delimiter = null;

    @Override
    public Data execute(Context context, Data input) {
        final TextList textList = input.toTextListOrFail();
        return new TextList(new Supplier<Text>() {
            private Deque<Text> list = new ArrayDeque<Text>();

            @Override
            public Text get(Set<Hint> hints) {
                if (list.isEmpty()) {
                    Text text = textList.next(hints);
                    if (text != null) {
                        List<String> split;
                        if (delimiter == null) {
                            JSONArray arr = JsonUtils.toJsonArray(text.getText());
                            if (arr == null) {
                                split = StringUtils.splitNewline(text.getText());
                            } else {
                                split = new ArrayList<>(arr.length());
                                for (Object obj : arr) {
                                    split.add(Objects.toString(obj, ""));
                                }
                            }
                        } else {
                            split = StringUtils.split(text.getText(), delimiter);
                        }
                        for (String str : split) {
                            list.add(new Text(str));
                        }
                    }
                }
                return (list.isEmpty() ? null : list.removeFirst());
            }
        });
    }
}
