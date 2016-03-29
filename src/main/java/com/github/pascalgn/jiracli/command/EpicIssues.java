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

import java.util.ArrayDeque;
import java.util.Deque;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "epicissues", "epici" }, description = "List all issues associated with the given epic")
class EpicIssues implements Command {
    @Override
    public IssueList execute(Context context, Data input) {
        IssueList issueList = input.toIssueList();
        return new IssueList(new IssueSupplier(context, issueList));
    }

    private static class IssueSupplier implements Supplier<Issue> {
        private final Context context;
        private final IssueList input;

        private final Deque<Issue> issues;

        public IssueSupplier(Context context, IssueList input) {
            this.context = context;
            this.input = input;
            this.issues = new ArrayDeque<Issue>();
        }

        @Override
        public synchronized Issue get() {
            if (issues.isEmpty()) {
                Issue next = input.next();
                if (next != null) {
                    issues.addAll(context.getWebService().getEpicIssues(next));
                }
            }
            return (issues.isEmpty() ? null : issues.removeFirst());
        }
    }
}
