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

import java.util.ArrayList;
import java.util.List;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Function;

@CommandDescription(names = "update", description = "Update the given issues on the server")
class Update implements Command {
    @Argument(names = { "-n", "--dry" }, description = "only print modified fields")
    private boolean dry;

    @Argument(names = { "-E", "--no-email" },
            description = "disable email notifications, requires project admin privileges")
    private boolean noEmail;

    @Override
    public Data execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        if (dry) {
            List<Text> texts = new ArrayList<>();
            Issue issue;
            while ((issue = issueList.next()) != null) {
                for (Field field : issue.getFieldMap().getLoadedFields()) {
                    Value value = field.getValue();
                    if (value.modified()) {
                        texts.add(new Text(field.getId() + " = " + value.get()));
                    }
                }
            }
            return new TextList(texts.iterator());
        } else {
            final boolean notifyUsers = !noEmail;
            return new IssueList(issueList.convertingSupplier(new Function<Issue, Issue>() {
                @Override
                public Issue apply(Issue issue) {
                    context.getWebService().updateIssue(issue, notifyUsers);
                    return issue;
                }
            }));
        }
    }
}
