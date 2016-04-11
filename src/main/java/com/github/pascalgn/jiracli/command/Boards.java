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

import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Board;
import com.github.pascalgn.jiracli.model.BoardList;
import com.github.pascalgn.jiracli.model.Data;

@CommandDescription(names = "boards", description = "Show the existing boards")
class Boards implements Command {
    @Argument(names = { "-b", "--board" }, parameters = Parameters.ONE, variable = "<board>",
            description = "only show boards matching the given ID")
    private Integer board;

    @Argument(names = { "-n", "--name" }, parameters = Parameters.ONE, variable = "<name>",
            description = "only show boards matching the given name")
    private String name;

    @Override
    public BoardList execute(Context context, Data input) {
        if (board == null) {
            List<Board> boards;
            if (name == null) {
                boards = context.getWebService().getBoards();
            } else {
                boards = context.getWebService().getBoards(name);
            }
            return new BoardList(boards.iterator());
        } else {
            Board b = context.getWebService().getBoard(board.intValue());
            return new BoardList(b);
        }
    }
}
