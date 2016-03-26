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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.IssueListType;
import com.github.pascalgn.jiracli.model.None;

@CommandDescription(names = { "print", "p" }, description = "Print the given JIRA issues using the given format")
class Print implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Print.class);

    private static final String DEFAULT_PATTERN = "${key} - ${summary}";
    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Argument(names = "-n", description = "don't print a newline at the end")
    private boolean noNewline = false;

    @Argument(parameters = Parameters.ZERO_OR_ONE, variable = "<format>", description = "the print format")
    private String pattern = DEFAULT_PATTERN;

    public Print() {
        // default constructor
    }

    Print(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public None execute(Context context, Data<?> input) {
        IssueList issueList = (IssueList) input.convertTo(IssueListType.getInstance());

        Issue issue;
        while ((issue = issueList.next()) != null) {
            String str;
            try {
                str = toString(context.getWebService(), issue, pattern);
            } catch (RuntimeException e) {
                LOGGER.debug("Error while reading issue: {}", issue.getKey(), e);
                str = "[Invalid issue: " + e.getLocalizedMessage() + " - " + issue.getKey() + "]";
            }
            if (noNewline) {
                context.getConsole().print(str);
            } else {
                context.getConsole().println(str);
            }
        }
        return null;
    }

    private static String toString(WebService webService, Issue issue, String pattern) {
        JSONObject json = webService.getIssue(issue.getKey());

        StringBuilder str = new StringBuilder();
        Matcher m = PATTERN.matcher(pattern);
        int end = 0;
        while (m.find()) {
            str.append(pattern.substring(end, m.start()));
            end = m.end();

            String name = m.group(1);
            str.append(CommandUtils.getFieldValue(webService, json, name));
        }

        str.append(pattern.substring(end));

        return str.toString();
    }
}
