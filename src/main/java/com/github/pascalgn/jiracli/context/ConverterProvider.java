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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.util.JsonUtils;
import com.github.pascalgn.jiracli.util.StringUtils;

class ConverterProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConverterProvider.class);

    private static final ConverterProvider INSTANCE = new ConverterProvider();

    public static Converter getConverter(JSONObject schema) {
        return INSTANCE.get(schema);
    }

    private final Map<String, Converter> mappedBySystem;
    private final Map<String, Converter> mappedByCustom;
    private final Converter defaultConverter;

    private ConverterProvider() {
        Converter string = new StringConverter();
        Converter datetime = new StringConverter();
        Converter number = new NumberConverter();
        Converter array = new ArrayConverter();
        Converter object = new ObjectConverter();
        Converter named = new NamedObjectConverter("name");
        Converter project = new NamedObjectConverter("key");

        mappedBySystem = new HashMap<>();
        mappedBySystem.put("labels", array);
        mappedBySystem.put("watches", object);
        mappedBySystem.put("worklog", object);
        mappedBySystem.put("subtasks", array);
        mappedBySystem.put("versions", array);
        mappedBySystem.put("issuelinks", array);
        mappedBySystem.put("fixVersions", array);
        mappedBySystem.put("attachment", array);
        mappedBySystem.put("components", array);
        mappedBySystem.put("votes", object);
        mappedBySystem.put("comment", object);
        mappedBySystem.put("issuetype", named);
        mappedBySystem.put("resolution", object);
        mappedBySystem.put("project", project);
        mappedBySystem.put("status", named);
        mappedBySystem.put("summary", string);
        mappedBySystem.put("description", string);
        mappedBySystem.put("duedate", string);
        mappedBySystem.put("created", datetime);
        mappedBySystem.put("updated", datetime);
        mappedBySystem.put("resolutiondate", string);
        mappedBySystem.put("lastViewed", string);
        mappedBySystem.put("environment", string);
        mappedBySystem.put("creator", named);
        mappedBySystem.put("assignee", named);
        mappedBySystem.put("reporter", named);
        mappedBySystem.put("timetracking", object);
        mappedBySystem.put("priority", named);
        mappedBySystem.put("progress", object);
        mappedBySystem.put("aggregateprogress", object);
        mappedBySystem.put("workratio", number);
        mappedBySystem.put("timespent", number);
        mappedBySystem.put("aggregatetimespent", number);
        mappedBySystem.put("timeestimate", number);
        mappedBySystem.put("aggregatetimeestimate", number);
        mappedBySystem.put("timeoriginalestimate", number);
        mappedBySystem.put("aggregatetimeoriginalestimate", number);

        mappedByCustom = new HashMap<>();
        mappedByCustom.put("com.atlassian.jira.toolkit:message", string);
        mappedByCustom.put("com.atlassian.jira.toolkit:comments", string);
        mappedByCustom.put("com.atlassian.jira.ext.charting:timeinstatus", object);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:url", string);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:textfield", string);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:textarea", string);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:datepicker", string);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:datetime", string);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:float", number);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:importid", number);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:select", object);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:labels", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:multiselect", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:multiversion", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect", array);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:userpicker", object);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:grouppicker", object);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons", object);
        mappedByCustom.put("com.atlassian.jira.plugin.system.customfieldtypes:version", object);

        defaultConverter = new UnknownConverter();
    }

    private Converter get(final JSONObject schema) {
        if (schema == null) {
            return defaultConverter;
        }
        String system = schema.optString("system", "");
        Converter converter = mappedBySystem.get(system);
        if (converter == null) {
            String custom = schema.optString("custom", "");
            converter = mappedByCustom.get(custom);
        }
        if (converter == null) {
            LOGGER.trace("Unknown field: {}", schema);
            return defaultConverter;
        } else {
            return converter;
        }
    }

    private static class StringConverter extends AbstractConverter {
        @Override
        public Object fromString(String str) {
            return str;
        }
    }

    private static class ArrayConverter extends AbstractConverter {
        @Override
        public String toString(Object value) {
            if (value instanceof JSONArray) {
                JSONArray array = (JSONArray) value;
                if (array.length() == 0) {
                    return "";
                } else {
                    boolean allStrings = true;
                    for (Object obj : array) {
                        if (!(obj instanceof String)) {
                            allStrings = false;
                            break;
                        }
                    }
                    if (allStrings) {
                        return StringUtils.join(array, ", ");
                    } else {
                        return array.toString();
                    }
                }
            } else {
                return super.toString(value);
            }
        }

        @Override
        public Object fromString(String str) {
            JSONArray array = JsonUtils.toJsonArray(str);
            if (array == null) {
                array = new JSONArray();
                List<String> split = StringUtils.split(str, ",");
                for (String s : split) {
                    array.put(s.trim());
                }
            }
            return array;
        }
    }

    private static class ObjectConverter extends AbstractConverter {
        @Override
        public Object fromString(String str) {
            return new JSONObject(str);
        }
    }

    private static class NamedObjectConverter extends AbstractConverter {
        private final String name;

        public NamedObjectConverter(String name) {
            this.name = name;
        }

        @Override
        public String toString(Object value) {
            if (value instanceof JSONObject) {
                JSONObject obj = (JSONObject) value;
                return obj.optString(name);
            } else {
                return super.toString(value);
            }
        }

        @Override
        public Object fromString(String str) {
            JSONObject object = JsonUtils.toJsonObject(str);
            if (object == null) {
                return new JSONObject().put(name, str);
            } else {
                return object;
            }
        }
    }

    private static class NumberConverter extends AbstractConverter {
        @Override
        public Object fromString(String str) {
            return new BigDecimal(str);
        }
    }

    private static class UnknownConverter extends AbstractConverter {
        @Override
        public Object fromString(String str) {
            if (str.isEmpty()) {
                return "";
            }
            JSONObject object = JsonUtils.toJsonObject(str);
            if (object != null) {
                return object;
            }
            JSONArray array = JsonUtils.toJsonArray(str);
            if (array != null) {
                return array;
            }
            return str;
        }
    }

    private abstract static class AbstractConverter implements Converter {
        @Override
        public String toString(Object value) {
            if (value == null || value == JSONObject.NULL) {
                return "";
            } else {
                return value.toString();
            }
        }
    }
}
