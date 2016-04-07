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

public class Sprint extends Data {
    private final Board board;

    private final int id;
    private final String name;

    public Sprint(Board board, int id, String name) {
        this.board = board;
        this.id = id;
        this.name = name;
    }

    public Board getBoard() {
        return board;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public Text toText() {
        return new Text(getName());
    }

    @Override
    public TextList toTextList() {
        return new TextList(toText());
    }

    @Override
    public String toString() {
        return "Sprint[id=" + id + ", name=" + name + "]";
    }
}
