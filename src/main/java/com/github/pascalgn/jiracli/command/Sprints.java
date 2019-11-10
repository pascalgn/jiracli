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

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.BoardList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Filter;
import com.github.pascalgn.jiracli.model.Sprint;
import com.github.pascalgn.jiracli.model.Sprint.State;
import com.github.pascalgn.jiracli.model.SprintList;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "sprints", description = "Show the sprints for the given boards")
class Sprints implements Command {
    private static final Filter<Sprint> ANY = new Filter<Sprint>() {
        @Override
        public Sprint get(Supplier<Sprint> supplier, Set<Hint> hints) {
            return supplier.get(hints);
        }
    };

    @Argument(names = { "-s", "--sprint" }, parameters = Parameters.ONE, variable = "<sprint>",
            description = "only show sprints matching the given ID")
    private Integer sprint;

    @Argument(names = { "-t", "--state" }, parameters = Parameters.ONE, variable = "<state>",
            description = "only show sprints with the given state")
    private String state = State.ACTIVE.name();

    @Override
    public SprintList execute(final Context context, Data input) {
        if (sprint == null) {
            return listSprints(context, input);
        } else {
            Sprint s = context.getWebService().getSprint(sprint);
            return new SprintList(s);
        }
    }

    private SprintList listSprints(final Context context, Data input) {
        Filter<Sprint> filter;
        if (state == null || state.equals("*") || state.equalsIgnoreCase("any")) {
            filter = ANY;
        } else {
            final State s = State.valueOf(state.toUpperCase());
            if (s == null) {
                throw new IllegalArgumentException("Unknown state: " + state);
            }
            filter = new Filter<Sprint>() {
                @Override
                public Sprint get(Supplier<Sprint> supplier, Set<Hint> hints) {
                    Sprint next;
                    while ((next = supplier.get(hints)) != null) {
                        if (next.getState() == s) {
                            return next;
                        }
                    }
                    return null;
                }
            };
        }

        BoardList boardList = input.toBoardListOrFail();
        SprintList sprintList = new SprintList(boardList.loadingSupplier(new Function<Board, Collection<Sprint>>() {
            @Override
            public Collection<Sprint> apply(Board board, Set<Hint> hints) {
                return context.getWebService().getSprints(board);
            }
        }));

        return sprintList.filteredList(filter);
    }
}
