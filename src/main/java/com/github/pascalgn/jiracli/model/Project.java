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

import java.util.List;

public class Project extends Data {
    private final int id;
    private final String key;
    private final String name;

    private final List<IssueType> issueTypes;

    public Project(int id, String key, String name, List<IssueType> issueTypes) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.issueTypes = issueTypes;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<IssueType> getIssueTypes() {
        return issueTypes;
    }

    @Override
    public Text toText() {
        return new Text(getKey());
    }

    @Override
    public TextList toTextList() {
        return new TextList(toText());
    }

    @Override
    public String toString() {
        return "Project[id=" + id + ", key=" + key + ", name=" + name + "]";
    }
}
