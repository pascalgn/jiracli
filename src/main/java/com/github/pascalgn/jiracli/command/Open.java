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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "open", description = "Open the text content in an editor")
class Open implements Command {
    @Argument(names = { "-s", "--suffix" }, parameters = Parameters.ONE, variable = "<suffix>",
            description = "the file suffix to use")
    private String fileSuffix;

    @Argument(names = { "-e", "--encoding" }, parameters = Parameters.ONE, variable = "<encoding>",
            description = "the file encoding to use")
    private String encoding;

    @Argument(names = { "-u", "--unicode" }, description = "use UTF-8 encoding, short for -e UTF-8")
    private boolean unicode;

    @Override
    public Data execute(Context context, Data input) {
        Text text = input.toTextOrFail();
        String str = text.getText();

        Charset charset = StandardCharsets.UTF_8;

        String suffix;
        if (fileSuffix == null) {
            String contentType = text.getType();
            if (contentType.equals("application/json")) {
                suffix = ".json";
            } else if (contentType.equals("text/csv")) {
                suffix = ".csv";
                charset = StandardCharsets.ISO_8859_1;
            } else {
                if (JsonUtils.toJsonObject(str) != null || JsonUtils.toJsonArray(str) != null) {
                    suffix = ".json";
                } else if (str.startsWith("### ") || str.startsWith("--- ")) {
                    suffix = ".diff";
                } else {
                    suffix = ".txt";
                }
            }
        } else {
            suffix = fileSuffix;
        }

        if (unicode) {
            charset = StandardCharsets.UTF_8;
        } else if (encoding != null) {
            charset = Charset.forName(encoding);
        }

        File tempFile;
        try {
            tempFile = File.createTempFile("open", suffix);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temporary file!", e);
        }

        tempFile.deleteOnExit();
        IOUtils.write(tempFile, charset, str);

        context.getConsole().openFile(tempFile);

        return None.getInstance();
    }
}
