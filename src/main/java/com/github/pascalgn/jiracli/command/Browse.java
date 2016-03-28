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

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.util.Function;

@CommandDescription(names = "browse", description = "Open the given JIRA issues in the system's default browser")
class Browse implements Command {
    @Argument(names = "-n", description = "only print the URLs, don't open a browser")
    private boolean dryRun = false;

    @Override
    public Data execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        if (dryRun) {
            return issueList.toTextList(new Function<Issue, Text>() {
                @Override
                public Text apply(Issue issue) {
                    URI uri = context.getWebService().getURI(issue.getKey());
                    return new Text(uri.toString());
                }
            });
        } else {
            Desktop desktop = Desktop.getDesktop();

            Issue issue;
            while ((issue = issueList.next()) != null) {
                URI uri = context.getWebService().getURI(issue.getKey());
                try {
                    desktop.browse(uri);
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot open URL: " + uri, e);
                }
            }
            return input;
        }
    }
}
