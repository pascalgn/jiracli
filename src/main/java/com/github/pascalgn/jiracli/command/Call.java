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

import java.util.Objects;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.WebService.Method;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;

@CommandDescription(names = "call", description = "Call the given URL")
class Call implements Command {
    @Argument(names = { "-m", "--method" }, parameters = Parameters.ONE, variable = "<method>",
            description = "The method, one of GET, POST, PUT, DELETE")
    private String method = "GET";

    @Argument(parameters = Parameters.ONE, variable = "<path>", description = "The path, relative to the root URL")
    private String path;

    @Override
    public Text execute(Context context, Data input) {
        Text text = input.toText();
        String body = (text == null ? null : text.getText());
        String response = context.getWebService().execute(toMethod(method), path, body);
        return new Text(response);
    }

    private static Method toMethod(String str) {
        String s = Objects.toString(str, "").trim().toUpperCase();
        Method m = Method.valueOf(s);
        if (m == null) {
            throw new IllegalArgumentException("Unknown method: " + str);
        }
        return m;
    }
}
