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
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Comment;
import com.github.pascalgn.jiracli.model.CommentList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.ConversionUtils;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "comments", description = "Show the comments of the given issues")
class Comments implements Command {
    @Argument(names = { "-j", "--json" }, description = "return a json representation of the comments")
    private boolean json;

    @Override
    public Data execute(final Context context, Data input) {
        IssueList issueList = input.toIssueListOrFail();
        Set<Hint> hints = IssueHint.fields("comment");
        if (json) {
            return new TextList(issueList.loadingSupplier(hints, new Function<Issue, Collection<Text>>() {
                @Override
                public Collection<Text> apply(Issue issue, Set<Hint> hints) {
                    List<Comment> comments = context.getWebService().getComments(issue);
                    Collection<Text> texts = new ArrayList<>();
                    for (Comment comment : comments) {
                        texts.add(new Text(ConversionUtils.toJson(comment).toString()));
                    }
                    return texts;
                }
            }));
        } else {
            return new CommentList(issueList.loadingSupplier(hints, new Function<Issue, Collection<Comment>>() {
                @Override
                public Collection<Comment> apply(Issue issue, Set<Hint> hints) {
                    return context.getWebService().getComments(issue);
                }
            }));
        }
    }
}
