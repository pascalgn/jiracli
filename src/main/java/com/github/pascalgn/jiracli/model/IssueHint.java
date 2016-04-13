package com.github.pascalgn.jiracli.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.pascalgn.jiracli.util.Hint;

public class IssueHint extends Hint {
    /**
     * Indicates that no fields are needed, only the number of issues
     */
    public static Set<Hint> count() {
        return Collections.<Hint> singleton(Count.INSTANCE);
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
            throw new IllegalArgumentException("Empty fields: " + fields);
        }
        Set<Hint> hints = new HashSet<>();
        for (String field : fields) {
            hints.add(new Field(field));
        }
        return hints;
    }

    /**
     * Indicates that all editable fields are needed
     */
    public static Set<Hint> editableFields() {
        return Collections.<Hint> singleton(EditableFields.INSTANCE);
    }

    /**
     * Returns all fields that are proposed by the given hints
     */
    public static Set<String> getFields(Set<Hint> hints) {
        Set<String> fields = new HashSet<>();
        for (Hint hint : hints) {
            if (hint instanceof Field) {
                fields.add(((Field) hint).getField());
            }
        }
        return fields;
    }

    public static final class Count extends IssueHint {
        public static final Count INSTANCE = new Count();

        private Count() {
            // only allow one instance
        }

        @Override
        public String toString() {
            return "Count";
        }
    }

    public static final class Field extends IssueHint {
        private final String field;

        public Field(String field) {
            this.field = field;
        }

        public String getField() {
            return field;
        }

        @Override
        public int hashCode() {
            return 31 + field.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Field other = (Field) obj;
            return field.equals(other.field);
        }

        @Override
        public String toString() {
            return "Field[field=" + field + "]";
        }
    }

    public static final class EditableFields extends IssueHint {
        public static final EditableFields INSTANCE = new EditableFields();

        private EditableFields() {
            // only allow one instance
        }

        @Override
        public String toString() {
            return "EditableFields";
        }
    }
}
