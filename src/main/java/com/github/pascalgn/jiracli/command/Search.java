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

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.NoneType;
import com.github.pascalgn.jiracli.util.Supplier;

class Search implements Command<NoneType, None, IssueList> {
    private final String jql;

    public Search(String jql) {
        this.jql = jql;
    }

    @Override
    public NoneType getInputType() {
        return NoneType.getInstance();
    }

    @Override
    public IssueList execute(Context context, None input) {
        return new IssueList(new IssueSupplier(context, jql));
    }

    private static class IssueSupplier implements Supplier<Issue> {
        private Context context;
        private String jql;

        private List<Issue> issues;
        private int issuesIndex;

        public IssueSupplier(Context context, String jql) {
            this.context = context;
            this.jql = jql;
        }

        @Override
        public synchronized Issue get() {
            if (issues == null) {
                List<JSONObject> json = context.getWebService().searchIssues(jql);
                issues = new ArrayList<Issue>(json.size());
                for (JSONObject obj : json) {
                    issues.add(Issue.valueOf(obj.getString("key")));
                }
                issuesIndex = 0;
            }
            return (issuesIndex < issues.size() ? issues.get(issuesIndex++) : null);
        }
    }
}
