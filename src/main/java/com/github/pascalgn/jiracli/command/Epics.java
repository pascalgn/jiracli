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

import java.util.Collection;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.DefaultRequest;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.BoardList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "epics", description = "Show the epics for the given boards")
class Epics implements Command {
    @Override
    public Data execute(final Context context, Data input) {
        BoardList boardList = input.toBoardListOrFail();
        return new IssueList(boardList.loadingSupplier(Hint.none(), new Function<Board, Collection<Issue>>() {
            @Override
            public Collection<Issue> apply(Board board, Set<Hint> hints) {
                return context.getWebService().getEpics(board, new DefaultRequest());
            }
        }));
    }
}
