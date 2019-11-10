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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.command.CommandFactory.UsageException;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = { "javascript", "js" }, description = "Execute JavaScript code for the given issues")
class JavaScript implements Command {
    @Argument(names = "--file", variable = "<file>", parameters = Parameters.ONE,
            description = "the script file to read javascript from")
    private String file;

    @Argument(names = { "-l", "--list" }, description = "pass the input list to the script, not single elements")
    private boolean list;

    @Argument(variable = "<javascript>", description = "the javascript code", parameters = Parameters.ZERO_OR_ONE)
    private String js;

    public JavaScript() {
        // default constructor
    }

    JavaScript(String js, boolean list) {
        this.js = js;
        this.list = list;
    }

    @Override
    public TextList execute(final Context context, Data data) {
        if (js != null && file != null) {
            throw new UsageException("Either javascript or file must be given, not both!");
        } else if (js == null && file == null) {
            throw new UsageException("Either javascript or file must be given!");
        }

        final String script;
        if (file == null) {
            script = js.trim();
        } else {
            File f = IOUtils.getFile(this.file);
            if (!f.exists()) {
                throw new IllegalArgumentException("File not found: " + this.file);
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
                if (list) {
                    return context.getJavaScriptEngine().evaluate(script, textList);
                } else {
                    return new TextList(textList.loadingSupplier(new Function<Text, Collection<Text>>() {
                        @Override
                        public Collection<Text> apply(Text text, Set<Hint> hints) {
                            TextList result = context.getJavaScriptEngine().evaluate(script, text);
                            return result.remaining(hints);
                        }
                    }));
                }
            }
        } else {
            List<String> fields = CommandUtils.findJavaScriptFields(script);
            Set<Hint> hints = IssueHint.fields(fields);
            if (list) {
                if (!hints.isEmpty()) {
                    issueList = new IssueList(issueList.convertingSupplier(hints, new Function<Issue, Issue>() {
                        @Override
                        public Issue apply(Issue issue, Set<Hint> hints) {
                            return issue;
                        }
                    }));
                }
                return context.getJavaScriptEngine().evaluate(script, issueList);
            } else {
                return new TextList(issueList.loadingSupplier(hints, new Function<Issue, Collection<Text>>() {
                    @Override
                    public Collection<Text> apply(Issue issue, Set<Hint> hints) {
                        TextList result = context.getJavaScriptEngine().evaluate(script, issue);
                        return result.remaining(hints);
                    }
                }));
            }
        }
    }
}
