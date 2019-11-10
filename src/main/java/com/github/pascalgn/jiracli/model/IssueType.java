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

public class IssueType {
    private final int id;
    private final String name;
    private final boolean subtask;

    public IssueType(int id, String name, boolean subtask) {
        this.id = id;
        this.name = name;
        this.subtask = subtask;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isSubtask() {
        return subtask;
    }

    @Override
    public String toString() {
        return "IssueType[id=" + id + ", name=" + name + "]";
    }
}
