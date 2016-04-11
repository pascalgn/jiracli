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
package com.github.pascalgn.jiracli.context;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.model.Workflow;

class DefaultWebServiceCache implements WebService.Cache {
    private final Map<String, String> responseCache;
    private final Map<String, JSONObject> fieldCache;
    private final Map<String, Workflow> workflowCache;

    public DefaultWebServiceCache() {
        this.responseCache = new HashMap<>();
        this.fieldCache = new HashMap<>();
        this.workflowCache = new HashMap<>();
    }

    public String getResponse(String path) {
        return responseCache.get(path);
    }

    public void putResponse(String path, String response) {
        responseCache.put(path, response);
    }

    public JSONObject getFields(String key) {
        return fieldCache.get(key);
    }

    public void putFields(String key, JSONObject fields) {
        fieldCache.put(key, fields);
    }

    public Workflow getWorkflow(String key) {
        return workflowCache.get(key);
    }

    public void putWorkflow(String key, Workflow workflow) {
        workflowCache.put(key, workflow);
    }

    @Override
    public void clear() {
        responseCache.clear();
        fieldCache.clear();
        workflowCache.clear();
    }
}
