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

import java.net.URI;

public class Issue extends Data {
    private final String key;
    private final URI uri;
    private final FieldMap fieldMap;

    public Issue(String key, URI uri, FieldMap fieldMap) {
        this.key = key;
        this.uri = uri;
        this.fieldMap = fieldMap;
    }

    /**
     * @return The issue key, for example <code>ISSUE-123</code>
     */
    public String getKey() {
        return key;
    }

    /**
     * @return The URI of this issue, for example <code>https://jira.example.com/browse/ISSUE-123</code>
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return The fields of this issue, never null
     */
    public FieldMap getFieldMap() {
        return fieldMap;
    }

    /**
     * @return The JSON representation of this issue, never null
     */
    public String toJson() {
        return IssueHelper.toJson(this);
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
}
