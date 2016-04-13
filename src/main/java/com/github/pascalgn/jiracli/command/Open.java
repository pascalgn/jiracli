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

import java.io.File;
import java.io.IOException;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "open", description = "Open the text content in an editor")
class Open implements Command {
    @Argument(names = { "-s", "--suffix" }, parameters = Parameters.ONE, variable = "<suffix>",
            description = "the file suffix to use")
    private String fileSuffix;

    @Override
    public Data execute(Context context, Data input) {
        String str = input.toTextOrFail().getText();

        String suffix;
        if (fileSuffix == null) {
            if (JsonUtils.toJsonObject(str) != null || JsonUtils.toJsonArray(str) != null) {
                suffix = ".json";
            } else if (str.startsWith("### ") || str.startsWith("--- ")) {
                suffix = ".diff";
            } else {
                suffix = ".txt";
            }
        } else {
            suffix = fileSuffix;
        }

        File tempFile;
        try {
            tempFile = File.createTempFile("open", suffix);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temporary file!", e);
        }

        IOUtils.write(tempFile, str);
        tempFile.deleteOnExit();

        context.getConsole().openFile(tempFile);

        return None.getInstance();
    }
}
