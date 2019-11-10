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
package com.github.pascalgn.jiracli.model;

public class Sprint extends Data {
    public enum State {
        CLOSED, ACTIVE, FUTURE, UNKNOWN;
    }

    private final int id;
    private final String name;
    private final State state;

    public Sprint(int id, String name, State state) {
        this.id = id;
        this.name = name;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public State getState() {
        return state;
    }

    @Override
    public SprintList toSprintList() {
        return new SprintList(this);
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
