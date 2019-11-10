/*
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
import com.github.pascalgn.jiracli.context.WebService.Request;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.CollectingSupplier;
import com.github.pascalgn.jiracli.util.ConversionUtils;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.JsonUtils;

@CommandDescription(names = "parse", description = "Parse issue data from text")
class Parse implements Command {
    // Fetch multiple issues at once to reduce server requests
    private static final int ISSUE_FETCH_SIZE = 10;

    @Argument(names = { "-k", "--keys" }, description = "only parse issue keys")
    private boolean parseKeys;

    @Override
    public Data execute(final Context context, Data input) {
        final TextList textList = input.toTextListOrFail();
        if (parseKeys) {
            return new IssueList(new CollectingSupplier<Issue>() {
                @Override
                protected Collection<Issue> nextItems(Set<Hint> hints) {
                    List<String> keys = new ArrayList<String>();
                    Text text;
                    // don't use hints for the input, they only apply to the returned issues!
                    while ((text = textList.next(Hint.none())) != null) {
                        keys.addAll(CommandUtils.findIssues(text.getText()));
                        if (keys.size() >= ISSUE_FETCH_SIZE) {
                            break;
                        }
                    }
                    Request request = CommandUtils.getRequest(hints);
                    return (keys.isEmpty() ? null : context.getWebService().getIssues(keys, request));
                }
            });
        } else {
            return new IssueList(textList.loadingSupplier(new Function<Text, Collection<Issue>>() {
                @Override
                public Collection<Issue> apply(Text text, Set<Hint> hints) {
                    return parseJson(context, text.getText());
                }
            }));
        }
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
                    issues.add(ConversionUtils.toIssue(context.getWebService(), j));
                }
                return issues;
            }
        } else {
            Issue issue = ConversionUtils.toIssue(context.getWebService(), json);
            return Collections.singleton(issue);
        }
    }
}
