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
package com.github.pascalgn.jiracli.testutil;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.WebService;

public class MockWebService implements WebService {
    private final Map<String, JSONObject> issues;
    private Map<String, String> fieldMapping;

    public MockWebService() {
        issues = new HashMap<String, JSONObject>();
        fieldMapping = Collections.emptyMap();
    }

    @Override
    public URI getURI(String issue) {
        return URI.create("issue://" + issue);
    }

    @Override
    public JSONObject getIssue(String issue) {
        return issues.get(issue);
    }

    public void clearIssues() {
        issues.clear();
    }

    public void setIssue(String key, JSONObject issue) {
        issues.put(key, issue);
    }

    @Override
    public List<JSONObject> searchIssues(String jql) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, String> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(Map<String, String> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    @Override
    public void close() {
    }
}
