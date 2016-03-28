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

import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;

@CommandDescription(names = { "base64", "b64" }, description = "Print text from standard input as Base64 encoded")
class Base64 implements Command {
    @Override
    public None execute(Context context, Data input) {
        Console console = context.getConsole();
        String raw;
        while ((raw = console.readLine()) != null) {
            String line = raw.trim();
            if (line.isEmpty()) {
                break;
            }
            String base64 = DatatypeConverter.printBase64Binary(line.getBytes(StandardCharsets.UTF_8));
            console.println(base64);
        }
        return None.getInstance();
    }
}
