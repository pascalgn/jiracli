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
package com.github.pascalgn.jiracli.model;

import java.util.Arrays;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Supplier;

public class BoardList extends List<Board> {
    public BoardList() {
        super();
    }

    public BoardList(Board... boards) {
        super(Arrays.asList(boards).iterator());
    }

    public BoardList(Iterator<Board> iterator) {
        super(iterator);
    }

    public BoardList(Supplier<Board> supplier) {
        super(supplier);
    }

    @Override
    public TextList toTextList() {
        return toTextList(new Function<Board, Text>() {
            @Override
            public Text apply(Board board) {
                return board.toText();
            }
        });
    }

    @Override
    public BoardList toBoardList() {
        return this;
    }

    @Override
    public BoardList filteredList(Filter<Board> filter) {
        return new BoardList(new FilteredSupplier<>(getSupplier(), filter));
    }
}