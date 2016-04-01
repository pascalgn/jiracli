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

public class Field extends Data {
    private final Issue issue;

    private final String id;
    private final String name;

    private final Value value;

    public Field(Issue issue, String id, String name, Value value) {
        this.issue = issue;
        this.id = id;
        this.name = name;
        this.value = value;
    }

    public Issue getIssue() {
        return issue;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public Issue toIssue() {
        return issue;
    }

    @Override
    public IssueList toIssueList() {
        return issue.toIssueList();
    }

    @Override
    public Text toText() {
        return new Text(toString());
    }

    @Override
    public TextList toTextList() {
        return toText().toTextList();
    }

    @Override
    public Field toField() {
        return this;
    }

    @Override
    public FieldList toFieldList() {
        return new FieldList(this);
    }

    @Override
    public String toString() {
        return "Field[" + name + " = " + value + "]";
    }
}
