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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "split", description = "Split the input texts into multiple texts")
class Split implements Command {
    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<delimiter>", description = "the delimiter to use for splitting")
    private String delimiter = null;

    @Override
    public Data execute(Context context, Data input) {
        final TextList textList = input.toTextListOrFail();
        return new TextList(new Supplier<Text>() {
            private Deque<Text> list = new ArrayDeque<Text>();

            @Override
            public Text get() {
                if (list.isEmpty()) {
                    Text text = textList.next();
                    if (text != null) {
                        if (delimiter == null) {
                            splitNewline(text.getText(), list);
                        } else {
                            split(text.getText(), delimiter, list);
                        }
                    }
                }
                return (list.isEmpty() ? null : list.removeFirst());
            }
        });
    }

    static void splitNewline(String str, Collection<Text> result) {
        try (BufferedReader reader = new BufferedReader(new StringReader(str))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(new Text(line));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static void split(String str, String delimiter, Collection<Text> result) {
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("Empty delimiter!");
        }
        int position = 0;
        while (position < str.length()) {
            int old = position;
            int pos = str.indexOf(delimiter, position);
            if (pos == -1) {
                break;
            } else {
                result.add(new Text(str.substring(old, pos)));
                position = pos + delimiter.length();
            }
        }
        if (position <= str.length()) {
            result.add(new Text(str.substring(position, str.length())));
        }
    }
}
