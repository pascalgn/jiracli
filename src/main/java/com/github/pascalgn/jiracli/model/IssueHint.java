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
package com.github.pascalgn.jiracli.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.util.Hint;

public class IssueHint extends Hint {
    private static final IssueHint COUNT = new NamedHint("Count");
    private static final IssueHint CHANGELOG = new NamedHint("Changelog");
    private static final IssueHint ALL_FIELDS = new NamedHint("AllFields");
    private static final IssueHint EDITABLE_FIELDS = new NamedHint("EditableFields");

    /**
     * Indicates that no fields are needed, only the number of issues
     */
    public static IssueHint count() {
        return COUNT;
    }

    /**
     * Indicates that the given fields are needed
     */
    public static Set<Hint> fields(String... fields) {
        return fields(Arrays.asList(fields));
    }

    /**
     * Indicates that the given fields are needed
     */
    public static Set<Hint> fields(List<String> fields) {
        if (fields.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Hint> hints = new HashSet<>();
        for (String field : fields) {
            if (field.contains(".")) {
                field = field.split("\\.", 2)[0];
            }
            hints.add(new Field(field));
        }
        return hints;
    }

    /**
     * Indicates that the changelog is needed
     */
    public static IssueHint changelog() {
        return CHANGELOG;
    }

    /**
     * Indicates that all fields are needed
     */
    public static IssueHint allFields() {
        return ALL_FIELDS;
    }

    /**
     * Indicates that all editable fields are needed
     */
    public static IssueHint editableFields() {
        return EDITABLE_FIELDS;
    }

    /**
     * Returns all fields that are proposed by the given hints
     */
    public static Set<String> getFields(Set<Hint> hints) {
        Set<String> fields = new HashSet<>();
        for (Hint hint : hints) {
            if (hint instanceof Field) {
                fields.add(((Field) hint).getName());
            }
        }
        return fields;
    }

    public static final class Field extends NamedHint {
        public Field(String field) {
            super(field);
        }

        @Override
        public String toString() {
            return "Field[field=" + getName() + "]";
        }
    }

    private static class NamedHint extends IssueHint {
        private final String name;

        public NamedHint(String name) {
            this.name = name;
        }

        protected String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return 73 + name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            NamedHint other = (NamedHint) obj;
            return name.equals(other.name);
        }

        @Override
        public String toString() {
            return "NamedHint[name=" + name + "]";
        }
    }
}
