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

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "links", "linked" }, description = "Show all issues linked with the given issues")
class Links implements Command {
    @Override
    public Data execute(final Context context, Data input) {
        final IssueList issueList = input.toIssueListOrFail();
        final Set<Hint> issuelinks = IssueHint.fields("issuelinks");

        // Create a custom supplier, because the 'issuelinks' hint should not be passed to the children items
        Supplier<Issue> supplier = new Supplier<Issue>() {
            private Iterator<Issue> iterator;

            @Override
            public Issue get(Set<Hint> hints) {
                while (iterator == null || !iterator.hasNext()) {
                    Issue next = issueList.next(issuelinks);
                    if (next == null) {
                        break;
                    } else {
                        List<Issue> links = context.getWebService().getLinks(next, CommandUtils.getRequest(hints));
                        iterator = links.iterator();
                    }
                }
                return (iterator.hasNext() ? iterator.next() : null);
            }
        };

        return new IssueList(supplier);
    }
}
