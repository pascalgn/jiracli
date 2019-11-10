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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = "write", description = "Write the input to the given file")
class Write implements Command {
    @Argument(parameters = Parameters.ONE, variable = "<file>", description = "the file to write")
    private String filepath;

    @Override
    public None execute(Context context, Data input) {
        TextList textList = input.toTextListOrFail();
        File file = IOUtils.getFile(filepath);
        if (file.exists()) {
            throw new IllegalArgumentException("File exists: " + filepath);
        }
        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            Text text;
            while ((text = textList.next(Hint.none())) != null) {
                writer.append(text.getText());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error writing file: " + file, e);
        }
        return None.getInstance();
    }
}
