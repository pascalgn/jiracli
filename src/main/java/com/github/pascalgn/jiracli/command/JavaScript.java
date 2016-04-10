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
import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.command.CommandFactory.UsageException;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = { "javascript", "js" }, description = "Execute JavaScript code for the given issues")
class JavaScript implements Command {
    @Argument(names = "--file", variable = "<file>", parameters = Parameters.ONE,
            description = "the script file to read javascript from")
    private String file;

    @Argument(names = { "-f", "--fields" }, variable = "<fields>", parameters = Parameters.ONE_OR_MORE,
            description = "the fields available to javascript (by default, all loaded fields will be available)")
    private List<String> fields;

    @Argument(variable = "<javascript>", description = "the javascript code", parameters = Parameters.ZERO_OR_ONE)
    private String js;

    public JavaScript() {
        // default constructor
    }

    JavaScript(String js) {
        this.js = js;
    }

    @Override
    public Data execute(final Context context, Data data) {
        if (js != null && file != null) {
            throw new UsageException("Either js or file must be given, not both!");
        } else if (js == null && file == null) {
            throw new UsageException("Either js or file must be given!");
        }

        final String script;
        if (file == null) {
            script = js.trim();
        } else {
            File f = new File(this.file);
            if (!f.isAbsolute() || !f.exists()) {
                throw new UsageException("File not found: " + this.file);
            }
            script = IOUtils.toString(f).trim();
        }

        if (script.isEmpty()) {
            throw new UsageException("Script is empty!");
        }

        IssueList issueList = data.toIssueList();
        if (issueList == null) {
            TextList textList = data.toTextList();
            if (textList == null) {
                return context.getJavaScriptEngine().evaluate(script);
            } else {
                return context.getJavaScriptEngine().evaluate(script, textList);
            }
        } else {
            return context.getJavaScriptEngine().evaluate(script, issueList);
        }
    }
}
