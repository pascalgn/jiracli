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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "parent", description = "Get the parent of the given sub-task issues")
class Parent implements Command {
    @Override
    public IssueList execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        Set<Hint> hints = IssueHint.fields("parent");
        return new IssueList(issueList.loadingSupplier(hints, new Function<Issue, Collection<Issue>>() {
            @Override
            public Collection<Issue> apply(Issue issue, Set<Hint> hints) {
                Issue parent = context.getWebService().getParent(issue, CommandUtils.getRequest(hints));
                return (parent == null ? Collections.<Issue> emptyList() : Collections.singleton(parent));
            }
        }));
    }

}
