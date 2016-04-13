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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "print", "p" }, description = "Print the given JIRA issues using the given format")
class Print implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Print.class);

    private static final String DEFAULT_PATTERN = "$key - $summary";

    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<format>", description = "the print format")
    private String pattern = DEFAULT_PATTERN;

    public Print() {
        // default constructor
    }

    Print(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public TextList execute(final Context context, Data input) {
        IssueList issueList = input.toIssueList();
        if (issueList == null) {
            final Iterator<Data> iterator = input.toIterator(Hint.none());
            return new TextList(new Supplier<Text>() {
                @Override
                public Text get(Set<Hint> hints) {
                    if (iterator.hasNext()) {
                        Data data = iterator.next();
                        String str;
                        try {
                            str = CommandUtils.toString(data, pattern, "");
                        } catch (IllegalArgumentException e) {
                            str = "[Error: " + data + ": " + e.getLocalizedMessage() + "]";
                        }
                        return new Text(str);
                    } else {
                        return null;
                    }
                }
            });
        } else {
            List<String> fields = CommandUtils.getFields(pattern);
            final Schema schema = context.getWebService().getSchema();
            return new TextList(issueList.convertingSupplier(IssueHint.fields(fields), new Function<Issue, Text>() {
                @Override
                public Text apply(Issue issue, Set<Hint> hints) {
                    String str;
                    try {
                        str = CommandUtils.toString(issue, schema, pattern, "");
                    } catch (IllegalArgumentException e) {
                        LOGGER.trace("Error while reading issue: {}", issue, e);
                        str = "[Error: " + issue + ": " + e.getLocalizedMessage() + "]";
                    }
                    return new Text(str);
                }
            }));
        }
    }
}
