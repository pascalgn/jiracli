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
package com.github.pascalgn.jiracli.command;

import java.util.Objects;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.util.ReflectionUtils;
import com.github.pascalgn.jiracli.util.StringUtils;

class FormatHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormatHelper.class);

    private final Schema schema;

    public FormatHelper(Schema schema) {
        this.schema = schema;
    }

    public String format(Object object, String format) {
        StringBuilder str = new StringBuilder();
        Matcher m = CommandUtils.getPropertyPattern().matcher(format);
        int end = 0;
        while (m.find()) {
            str.append(format.substring(end, m.start()));
            end = m.end();
            String name = (m.group(1) == null ? m.group(2) : m.group(1));
            Object value = getValue(object, name);
            if (value instanceof JSONArray) {
                str.append(StringUtils.join((JSONArray) value, ", "));
            } else {
                str.append(value);
            }
        }
        str.append(format.substring(end));
        return str.toString();
    }

    public String getValue(Object object, String property) {
        return getValue(object, property, "", false);
    }

    public String getValue(Object object, String property, boolean raw) {
        return getValue(object, property, "", raw);
    }

    public String getValue(Object object, String property, String defaultValue, boolean raw) {
        Object value = getObjectValue(object, property, defaultValue, raw);
        return Objects.toString(value, "");
    }

    private Object getObjectValue(Object object, String property, String defaultValue, boolean raw) {
        if (property.contains(".")) {
            String[] properties = property.split("\\.");
            Object obj = object;
            try {
                for (int i = 0; i < properties.length - 1; i++) {
                    obj = getObjectValue(obj, properties[i], defaultValue, true);
                }
                return getObjectValue(obj, properties[properties.length - 1], defaultValue, raw);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Cannot get property: " + property + ": " + object, e);
            }
        } else {
            Object value;
            if (object instanceof Issue) {
                Issue issue = (Issue) object;
                value = getValue(issue, property, raw);
            } else if (object instanceof JSONObject) {
                JSONObject json = (JSONObject) object;
                value = json.opt(property);
                if (value == JSONObject.NULL) {
                    value = null;
                }
            } else {
                value = ReflectionUtils.getValue(object, property);
            }
            if (value == null) {
                value = defaultValue;
                if (value == null) {
                    throw new IllegalArgumentException("Cannot get property: " + property + ": " + object);
                }
            }
            return value;
        }
    }

    private Object getValue(Issue issue, String field, boolean raw) {
        if (field.equals("key")) {
            return issue.getKey();
        }

        FieldMap fieldMap = issue.getFieldMap();
        Field f = fieldMap.getField(field, schema);
        if (f == null) {
            return null;
        }

        Object value = f.getValue().get();
        if (raw) {
            return value;
        }

        Object original = value;
        try {
            Converter converter = schema.getConverter(f.getId());
            return converter.toString(value);
        } catch (RuntimeException e) {
            LOGGER.trace("Error converting field value: {}: {}", f.getId(), original, e);
            return original;
        }
    }
}
