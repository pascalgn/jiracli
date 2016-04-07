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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.StringUtils;

class CommandUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandUtils.class);

    private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\$([a-zA-Z]+[a-zA-Z0-9]*)");

    public static String toString(Issue issue, Schema schema, String pattern) {
        StringBuilder str = new StringBuilder();
        Matcher m = VARIABLE_PATTERN.matcher(pattern);
        int end = 0;
        while (m.find()) {
            str.append(pattern.substring(end, m.start()));
            end = m.end();

            String name = (m.group(1) == null ? m.group(2) : m.group(1));
            Object value = CommandUtils.getFieldValue(issue, schema, name);
            if (value instanceof JSONArray) {
                return StringUtils.join((JSONArray) value, ", ");
            } else {
                str.append(value);
            }
        }

        str.append(pattern.substring(end));

        return str.toString();
    }

    public static Object getFieldValue(Issue issue, final Schema schema, String name) {
        if (name.equalsIgnoreCase("key")) {
            return issue.getKey();
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

        FieldMap fieldMap = issue.getFieldMap();
        Field field = fieldMap.getFieldById(fieldNameOrId);
        if (field == null) {
            field = fieldMap.getFieldByName(fieldNameOrId, new Function<Field, String>() {
                @Override
                public String apply(Field field) {
                    return schema.getName(field);
                }
            });
        }
        if (field == null) {
            throw new IllegalArgumentException("No such field: " + fieldNameOrId);
        }

        Object value = field.getValue().get();
        if (subname.isEmpty()) {
            return value;
        } else {
            if (value instanceof JSONObject) {
                return getFieldValue((JSONObject) value, subname);
            } else {
                throw new IllegalArgumentException("Not a Json object: " + fieldNameOrId + ": " + value);
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
                throw new IllegalArgumentException("Name '" + name + "' not found: " + json.toString(2));
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

    public static <T> T withTemporaryFile(String prefix, String suffix, Function<File, T> function) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            try {
                return function.apply(tempFile);
            } finally {
                if (!tempFile.delete() && tempFile.exists()) {
                    LOGGER.warn("Could not delete temporary file: {}", tempFile);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
