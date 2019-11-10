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
package com.github.pascalgn.jiracli.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.context.WebService.Request;
import com.github.pascalgn.jiracli.model.Comment;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.User;
import com.github.pascalgn.jiracli.model.Value;

public class ConversionUtils {
    private static final String ALL_FIELDS = "*";
    private static final Request EMPTY_REQUEST = new Request() {
        @Override
        public boolean getAllFields() {
            return false;
        }

        @Override
        public Collection<String> getFields() {
            return Collections.emptyList();
        }

        @Override
        public Collection<String> getExpand() {
            return Collections.emptyList();
        }
    };

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

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
            fieldList = new ArrayList<>();
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
     * Returns the Json representation of the comment, never null
     */
    public static JSONObject toJson(Comment comment) {
        return new JSONObject().put("author", toJson(comment.getAuthor()))
                .put("created", formatDate(comment.getCreated())).put("body", comment.getBody());
    }

    /**
     * Returns the Json representation of the user, never null
     */
    public static JSONObject toJson(User user) {
        return new JSONObject().put("id", user.getId()).put("name", user.getName());
    }

    /**
     * Parses the date, expected to be in the format 2020-12-31T23:59:59.123Z
     */
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date: " + date, e);
        }
    }

    /**
     * Formats the date, returns a string in the format 2020-12-31T23:59:59.123Z
     */
    public static String formatDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

    /**
     * Returns the parsed issue, never null
     */
    public static Issue toIssue(WebService webService, JSONObject json) {
        String key = json.optString("key");
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Invalid Json, missing key: " + json);
        }
        Issue issue = webService.getIssue(key, EMPTY_REQUEST);
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

    /**
     * Returns the parsed user, never null
     */
    public static User toUser(JSONObject json) {
        String id = json.optString("key");
        if (id == null || id.isEmpty()) {
            id = json.getString("name");
        }
        String name = json.getString("displayName");
        return new User(id, name);
    }
}
