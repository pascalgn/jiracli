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

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;

@CommandDescription(names = { "base64", "b64" }, description = "Print text from standard input as Base64 encoded")
class Base64 implements Command {
    @Argument(names = { "-d", "--decode" }, description = "decode base64 text")
    private boolean decode = false;

    @Override
    public TextList execute(Context context, Data input) {
        TextList textList = input.toTextListOrFail();
        return textList.toTextList(new Function<Text, Text>() {
            @Override
            public Text apply(Text text) {
                if (decode) {
                    String plain = new String(DatatypeConverter.parseBase64Binary(text.getText()), StandardCharsets.UTF_8);
                    return new Text(plain);
                } else {
                    String base64 = DatatypeConverter.printBase64Binary(text.getText().getBytes(StandardCharsets.UTF_8));
                    return new Text(base64);
                }
            }
        });
    }
}
