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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.context.DefaultRequest;
import com.github.pascalgn.jiracli.context.WebService.Request;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.StringUtils;

class CommandUtils {
    private static final Pattern ISSUE_KEY_PATTERN = Pattern.compile("([A-Z][A-Z0-9]*)-([0-9]+)");

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}|\\$([a-zA-Z]+[a-zA-Z0-9\\.]*)");
    private static final Pattern JS_FIELD_PATTERN = Pattern.compile("\\.fields\\.([a-zA-Z][a-zA-Z0-9_]*)");

    /**
     * Parses the given field parameter, allowing fields separated by comma, for
     * example <code>summary description,key</code>
     */
    public static List<String> getFields(List<String> fields) {
        if (fields == null) {
            return null;
        }
        List<String> result = new ArrayList<>();
        for (String field : fields) {
            result.addAll(StringUtils.split(field, ","));
        }
        return result;
    }

    /**
     * Finds all field references in the given printf pattern
     */
    public static List<String> findPatternFields(String pattern) {
        List<String> fields = new ArrayList<>();
        Matcher m = PROPERTY_PATTERN.matcher(pattern);
        while (m.find()) {
            String name = (m.group(1) == null ? m.group(2) : m.group(1));
            if (name.contains(".")) {
                String[] names = name.split("\\.", 2);
                name = names[0];
            }
            fields.add(name);
        }
        return fields;
    }

    /**
     * Returns a pattern that matches properties, for example
     * <code>$name</code>, <code>${name}</code>, <code>$some.name</code>
     */
    public static Pattern getPropertyPattern() {
        return PROPERTY_PATTERN;
    }

    /**
     * Finds all field references in the given JavaScript
     */
    public static List<String> findJavaScriptFields(String script) {
        List<String> fields = new ArrayList<>();
        Matcher m = JS_FIELD_PATTERN.matcher(script);
        while (m.find()) {
            fields.add(m.group(1));
        }
        return fields;
    }

    /**
     * Returns all issue keys in the given string
     */
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

    /**
     * Returns a pattern that matches issue keys, for example "ISSUE-123", "A-1", "B12-34567890"
     */
    public static Pattern getKeyPattern() {
        return ISSUE_KEY_PATTERN;
    }

    /**
     * Returns a new {@link Request} instance based on the given hints
     */
    public static Request getRequest(Set<Hint> hints) {
        boolean allFields = hints.contains(IssueHint.allFields());
        Collection<String> fields = IssueHint.getFields(hints);
        Collection<String> expand = new HashSet<>();
        if (hints.contains(IssueHint.editableFields())) {
            expand.add("editmeta");
        }
        return new DefaultRequest(allFields, fields, expand);
    }
}
