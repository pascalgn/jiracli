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
package com.github.pascalgn.jiracli.command;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;

class CommandUtils {
    private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");

    public static void closeUnchecked(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new IllegalStateException("Exception while trying to close: " + closeable, e);
        }
    }

    public static String join(Iterable<?> items, String sep) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Object obj : items) {
            if (first) {
                first = false;
            } else {
                result.append(sep);
            }
            result.append(obj);
        }
        return result.toString();
    }

    public static String repeat(String str, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }

    public static Object getFieldValue(Issue issue, String name) {
        if (name.equalsIgnoreCase("key")) {
            return issue.getKey();
        } else if (name.equalsIgnoreCase("uri")) {
            return issue.getUri();
        }

        String fieldNameOrId;
        String subname;
        if (name.contains(".")) {
            String[] names = name.split("\\.", 2);
            fieldNameOrId = names[0];
            subname = names[1];
        } else {
            fieldNameOrId = name;
            subname = "";
        }

        Field field = issue.getFieldMap().getFieldByName(fieldNameOrId);
        if (field == null) {
            field = issue.getFieldMap().getFieldById(fieldNameOrId);
        }
        if (field == null) {
            throw new IllegalStateException("No such field: " + fieldNameOrId + ": " + issue);
        }

        Object value = field.getValue().getValue();
        if (subname.isEmpty()) {
            return value;
        } else {
            if (value instanceof JSONObject) {
                return getFieldValue((JSONObject) value, subname);
            } else {
                throw new IllegalStateException("Not a Json object: " + issue + "/" + fieldNameOrId + ": " + value);
            }
        }
    }

    private static Object getFieldValue(JSONObject json, String name) {
        if (name.contains(".")) {
            String[] names = name.split("\\.");
            Object obj = getFieldValue(json, names[0]);
            for (int i = 1; i < names.length; i++) {
                obj = ((JSONObject) obj).get(names[i]);
            }
            return obj;
        } else {
            if (json.has(name)) {
                return json.get(name);
            } else {
                throw new IllegalStateException("Name '" + name + "' not found: " + json.toString(2));
            }
        }
    }

    public static boolean isIssue(String str) {
        return ISSUE_KEY_PATTERN.matcher(str).matches();
    }

    public static void checkIssue(String str) {
        if (!isIssue(str)) {
            throw new IllegalArgumentException("Invalid issue key: " + str);
        }
    }

    public static List<String> findIssues(String str) {
        List<String> result = null;
        Matcher m = ISSUE_KEY_PATTERN.matcher(str);
        if (m.find()) {
            result = new ArrayList<>();
            result.add(m.group());
        } else {
            return Collections.emptyList();
        }
        while (m.find()) {
            result.add(m.group());
        }
        return result;
    }
}
