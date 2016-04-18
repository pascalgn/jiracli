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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.IssueUtils;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "parse", description = "Parse issue data from text")
class Parse implements Command {
    @Argument(names = { "-k", "--keys" }, description = "only parse issue keys")
    private boolean parseKeys;

    @Override
    public Data execute(final Context context, Data input) {
        TextList textList = input.toTextListOrFail();
        return new IssueList(textList.loadingSupplier(new Function<Text, Collection<Issue>>() {
            @Override
            public Collection<Issue> apply(Text text, Set<Hint> hints) {
                String str = text.getText();
                if (parseKeys) {
                    List<String> keys = CommandUtils.findIssues(str);
                    Set<String> fields = IssueHint.getFields(hints);
                    return context.getWebService().getIssues(keys, fields);
                } else {
                    return parseJson(context, str);
                }
            }
        }));
    }

    private Collection<Issue> parseJson(Context context, String str) {
        JSONObject json = JsonUtils.toJsonObject(str);
        if (json == null) {
            JSONArray arr = JsonUtils.toJsonArray(str);
            if (arr == null) {
                throw new IllegalArgumentException("Invalid Json: " + str);
            } else {
                List<Issue> issues = new ArrayList<>();
                for (Object obj : arr) {
                    if (!(obj instanceof JSONObject)) {
                        throw new IllegalArgumentException("Invalid Json: " + obj);
                    }
                    JSONObject j = (JSONObject) obj;
                    issues.add(IssueUtils.toIssue(context.getWebService(), j));
                }
                return issues;
            }
        } else {
            Issue issue = IssueUtils.toIssue(context.getWebService(), json);
            return Collections.singleton(issue);
        }
    }
}
