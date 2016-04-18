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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "link", description = "Link the issues to the given issue")
class Link implements Command {
    private static final List<String> EMPTY = Collections.emptyList();

    @Argument(order = 1, parameters = Parameters.ONE, variable = "<issue>",
            description = "the issue to link the other issues to")
    private String issue;

    @Argument(order = 2, parameters = Parameters.ONE, variable = "<type>",
            description = "the name of the link type")
    private String linkName;

    @Argument(names = { "-r", "--reverse" }, description = "create the reverse link between the issues")
    private boolean reverse;

    @Argument(names = { "-d", "--delete" }, description = "delete the link between the issues")
    private boolean delete;

    @Override
    public Data execute(Context context, Data input) {
        final WebService webService = context.getWebService();

        final Issue target = webService.getIssues(Collections.singletonList(issue), EMPTY).get(0);

        IssueList issueList = input.toIssueListOrFail();
        return new IssueList(issueList.convertingSupplier(new Function<Issue, Issue>() {
            @Override
            public Issue apply(Issue source, Set<Hint> hints) {
                Issue inward;
                Issue outward;
                if (reverse) {
                    inward = target;
                    outward = source;
                } else {
                    inward = source;
                    outward = target;
                }
                if (delete) {
                    webService.removeLink(inward, outward, linkName);
                } else {
                    webService.linkIssues(inward, outward, linkName);
                }
                return source;
            }
        }));
    }
}
