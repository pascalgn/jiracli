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
package com.github.pascalgn.jiracli.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Value;

public class IssueUtils {
    private static final String ALL_FIELDS = "*";
    private static final List<String> EMPTY_FIELDS = Collections.emptyList();

    /**
     * Returns the Json representation of the issue, never null. All loaded fields will be included.
     */
    public static JSONObject toJson(Issue issue) {
        return toJson(issue, null);
    }

    /**
     * Returns the Json representation of the issue, never null
     *
     * @param fields The fields to be contained in the Json
     */
    public static JSONObject toJson(Issue issue, List<String> fields) {
        Collection<Field> fieldList;
        FieldMap fieldMap = issue.getFieldMap();
        if (fields == null) {
            fieldList = fieldMap.getLoadedFields();
        } else if (fields.contains(ALL_FIELDS)) {
            fieldList = fieldMap.getFields();
        } else {
            fieldList = new ArrayList<Field>();
            for (String f : fields) {
                Field field = fieldMap.getFieldById(f);
                if (field != null) {
                    fieldList.add(field);
                }
            }
        }

        JSONObject jsonFields = new JSONObject();
        for (Field field : fieldList) {
            jsonFields.put(field.getId(), field.getValue().get());
        }

        JSONObject json = new JSONObject();
        json.put("key", issue.getKey());
        json.put("fields", jsonFields);
        return json;
    }

    /**
     * Returns the parsed issue, never null
     */
    public static Issue toIssue(WebService webService, JSONObject json) {
        String key = json.optString("key");
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Invalid Json, missing key: " + json);
        }
        Issue issue = getIssue(webService, key);
        JSONObject fields = json.optJSONObject("fields");
        if (fields != null) {
            FieldMap fieldMap = issue.getFieldMap();
            for (String id : fields.keySet()) {
                Object val = fields.get(id);
                if (val == JSONObject.NULL) {
                    val = null;
                }
                Field field = fieldMap.getFieldById(id);
                if (field == null) {
                    field = new Field(issue, id, new Value(val));
                    fieldMap.addField(field);
                } else if (!Objects.equals(val, field.getValue().get())) {
                    field.getValue().set(val);
                }
            }
        }
        return issue;
    }

    private static Issue getIssue(WebService webService, String key) {
        List<Issue> issues = webService.getIssues(Collections.singletonList(key), EMPTY_FIELDS);
        if (issues.size() != 1) {
            throw new IllegalStateException("Invalid result for key: " + key);
        }
        return issues.get(0);
    }
}
