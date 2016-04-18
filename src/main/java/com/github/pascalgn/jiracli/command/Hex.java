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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "hex", description = "Display the hex value of non-printable characters")
class Hex implements Command {
    private static final Pattern PATTERN = Pattern.compile("[^ -~]");

    @Argument(names = { "-n", "--newlines" }, description = "also escape newline characters")
    private boolean newlines;

    @Argument(names = { "-h", "--hex" }, description = "output hex escapes even for \\t, \\r and \\n")
    private boolean hex;

    @Argument(names = { "-s", "--space" }, description = "output a dot instead of a space character")
    private boolean space;

    @Override
    public Data execute(Context context, Data input) {
        final Function<String, String> toHex = new ToHex();
        TextList textList = input.toTextListOrFail();
        return new TextList(textList.convertingSupplier(new Function<Text, Text>() {
            @Override
            public Text apply(Text text, Set<Hint> hints) {
                String str = text.getText();
                if (space) {
                    str = str.replace(' ', '.');
                }
                return new Text(replace(str, PATTERN, toHex));
            }
        }));
    }

    private static String replace(String str, Pattern pattern, Function<String, String> replace) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = pattern.matcher(str);
        int end = 0;
        while (matcher.find()) {
            sb.append(str.substring(end, matcher.start()));
            end = matcher.end();
            sb.append(replace.apply(matcher.group(), Hint.none()));
        }
        sb.append(str.substring(end));
        return sb.toString();
    }

    private class ToHex implements Function<String, String> {
        @Override
        public String apply(String str, Set<Hint> hints) {
            if (!newlines && (str.equals("\r") || str.equals("\n"))) {
                return str;
            }
            if (!hex) {
                if (str.equals("\t")) {
                    return "\\t";
                } else if (str.equals("\r")) {
                    return "\\r";
                } else if (str.equals("\n")) {
                    return "\\n";
                }
            }
            return String.format("\\u%02X", new BigInteger(1, str.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
