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

public class Issue extends Data {
    private final String key;
    private final FieldMap fieldMap;

    public Issue(String key, FieldMap fieldMap) {
        this.key = key;
        this.fieldMap = fieldMap;
    }

    /**
     * @return The issue key, for example <code>ISSUE-123</code>
     */
    public String getKey() {
        return key;
    }

    /**
     * @return The fields of this issue, never null
     */
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    @Override
    public Issue toIssue() {
        return this;
    }

    @Override
    public IssueList toIssueList() {
        return new IssueList(this);
    }

    @Override
    public Text toText() {
        return new Text(key);
    }

    @Override
    public TextList toTextList() {
        return new TextList(toText());
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * fieldMap.hashCode() + key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Issue other = (Issue) obj;
        return key.equals(other.key) && fieldMap.equals(other.fieldMap);
    }
}
