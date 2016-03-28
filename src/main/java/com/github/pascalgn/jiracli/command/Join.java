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
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;

@CommandDescription(names = "join", description = "Join the input texts into a single text")
class Join implements Command {
    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<separator>", description = "the string between the texts")
    private String separator = "";

    @Override
    public Data execute(Context context, Data input) {
        TextList textList = input.toTextListOrFail();

        StringBuilder str = new StringBuilder();
        Text text;
        while ((text = textList.next()) != null) {
            if (str.length() > 0) {
                str.append(separator);
            }
            str.append(text.getText());
        }

        return new Text(str.toString());
    }
}
