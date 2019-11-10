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

import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.StringUtils;

@CommandDescription(names = "echo", description = "Print the given text")
class Echo implements Command {
    @Argument(parameters = Parameters.ZERO_OR_MORE, variable = "<text>", description = "the text")
    private List<String> text;

    @Override
    public TextList execute(Context context, Data input) {
        if (text == null || text.isEmpty()) {
            return new TextList();
        } else {
            return new TextList(new Text(StringUtils.join(text, " ")));
        }
    }
}
