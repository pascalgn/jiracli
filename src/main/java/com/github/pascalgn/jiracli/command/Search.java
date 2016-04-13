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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "search", "s" }, description = "Search for issues via JQL")
class Search implements Command {
    @Argument(variable = "<jql>", description = "the JQL to search", parameters = Parameters.ONE)
    private String jql;

    @Argument(names = { "-f", "--fields" }, parameters = Parameters.ONE_OR_MORE, variable = "<field>",
            description = "issue fields to load initially")
    private List<String> fields = Collections.emptyList();

    public Search() {
        // default constructor
    }

    Search(String jql) {
        this.jql = jql;
    }

    @Override
    public IssueList execute(Context context, Data input) {
        return new IssueList(new IssueSupplier(context));
    }

    private class IssueSupplier implements Supplier<Issue> {
        private Context context;
        private Iterator<Issue> issues;

        public IssueSupplier(Context context) {
            this.context = context;
        }

        @Override
        public synchronized Issue get(Set<Hint> hints) {
            if (issues == null) {
                Set<String> hintFields = IssueHint.getFields(hints);
                hintFields.addAll(fields);
                List<Issue> list = context.getWebService().searchIssues(jql, hintFields);
                issues = list.iterator();
            }
            return (issues.hasNext() ? issues.next() : null);
        }
    }
}
