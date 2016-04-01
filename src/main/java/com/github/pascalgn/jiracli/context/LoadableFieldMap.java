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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.DefaultWebService.IssueData;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Value;
import com.github.pascalgn.jiracli.util.Supplier;

class LoadableFieldMap implements FieldMap {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadableFieldMap.class);

    private final IssueData issueData;
    private final Supplier<Map<String, JSONObject>> fieldData;
    private Issue issue;

    private final Map<String, Field> fields;
    private boolean loaded;

    public LoadableFieldMap(IssueData issueData, Supplier<Map<String, JSONObject>> fieldData) {
        this.issueData = issueData;
        this.fieldData = fieldData;
        this.fields = new HashMap<>();
    }

    void setIssue(Issue issue) {
        this.issue = issue;
        createFields(issueData.getInitialFields());
    }

    private void createFields(JSONObject json) {
        if (json != null) {
            for (String id : json.keySet()) {
                JSONObject data = fieldData.get().get(id);
                String name;
                JSONObject schema;
                if (data == null) {
                    LOGGER.info("Unknown field: {} (issue {})", id, issue);
                    name = id;
                    schema = null;
                } else {
                    name = data.optString("name");
                    if (name == null || name.isEmpty()) {
                        name = id;
                    }
                    schema = data.optJSONObject("schema");
                }
                Value value = ValueFactory.createValue(json.get(id), schema);
                fields.put(id, new Field(issue, id, name, value));
            }
        }
    }

    @Override
    public synchronized Field getFieldById(String id) {
        Field field = fields.get(id);
        if (field == null) {
            loadFields();
            field = fields.get(id);
        }
        return field;
    }

    @Override
    public synchronized Field getFieldByName(String name) {
        Field field = getFieldByName(fields.values(), name);
        if (field == null) {
            loadFields();
            field = getFieldByName(fields.values(), name);
        }
        return field;
    }

    private static Field getFieldByName(Collection<Field> fields, String name) {
        String lower = name.toLowerCase();
        for (Field field : fields) {
            if (field.getName().toLowerCase().equals(lower)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public synchronized Collection<Field> getFields() {
        loadFields();
        return fields.values();
    }

    private void loadFields() {
        if (!loaded) {
            JSONObject allFields = issueData.getAllFields(true);
            createFields(allFields);
            loaded = true;
        }
    }

    @Override
    public Collection<Field> getEditableFields() {
        return Collections.emptyList();
    }
}
