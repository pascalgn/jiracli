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
import com.github.pascalgn.jiracli.model.IssueListType;
import com.github.pascalgn.jiracli.model.None;

class JavaScript implements Command<IssueListType, IssueList, None> {
    private final String js;

    public JavaScript(String js) {
        this.js = js;
    }

    @Override
    public IssueListType getInputType() {
        return IssueListType.getInstance();
    }

    @Override
    public None execute(Context context, IssueList input) {
        List<Object> issues = new ArrayList<Object>();

        Issue issue;
        while ((issue = input.next()) != null) {
            JSONObject json = context.getWebService().getIssue(issue.getKey());
            Object obj = context.getJavaScriptEngine().evaluate("JSON.parse(input)", json.toString());
            issues.add(obj);
        }

        context.getJavaScriptEngine().evaluate(js, issues.toArray());
        return None.getInstance();
    }
}
