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
import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.BoardList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldList;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.SprintList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = { "issues", "i" },
        description = "List all given issues or the issues associated with the given epics or sprints")
class Issues implements Command {
    @Argument(parameters = Parameters.ZERO_OR_MORE, variable = "<issue>", description = "the issues")
    private List<String> issues;

    @Override
    public IssueList execute(final Context context, Data input) {
        if (issues == null) {
            SprintList sprintList = input.toSprintList();
            if (sprintList == null) {
                BoardList boardList = input.toBoardList();
                if (boardList == null) {
                    final FieldList fieldList = input.toFieldList();
                    if (fieldList == null) {
                        IssueList issueList = input.toIssueListOrFail();
                        return new IssueList(issueList.loadingSupplier(new Function<Issue, Collection<Issue>>() {
                            @Override
                            public Collection<Issue> apply(Issue issue) {
                                return context.getWebService().getIssues(issue);
                            }
                        }));
                    } else {
                        return new IssueList(new Supplier<Issue>() {
                            private Issue last;

                            @Override
                            public Issue get() {
                                Field field;
                                while ((field = fieldList.next()) != null) {
                                    Issue issue = field.getIssue();
                                    if (last == null || !last.equals(issue)) {
                                        last = issue;
                                        return issue;
                                    }
                                }
                                return null;
                            }
                        });
                    }
                } else {
                    return new IssueList(boardList.loadingSupplier(new Function<Board, Collection<Issue>>() {
                        @Override
                        public Collection<Issue> apply(Board board) {
                            return context.getWebService().getIssues(board);
                        }
                    }));
                }
            } else {
                return new IssueList(sprintList.loadingSupplier(new Function<Sprint, Collection<Issue>>() {
                    @Override
                    public Collection<Issue> apply(Sprint sprint) {
                        return context.getWebService().getIssues(sprint);
                    }
                }));
            }
        } else {
            List<Issue> issues = context.getWebService().getIssues(this.issues);
            return new IssueList(issues.iterator());
        }
    }
}
