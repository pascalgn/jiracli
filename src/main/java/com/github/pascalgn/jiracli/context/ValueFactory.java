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

import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.model.Value;

class ValueFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueFactory.class);

    public static Value createValue(Object value, JSONObject schema) {
        String valueType = value == null ? null : value.getClass().getName();

        Value result;
        try {
            result = doCreateValue(value, schema);
        } catch (RuntimeException e) {
            LOGGER.trace("Error creating value: {} (type: {}, schema: {})", value, valueType, schema, e);
            return new UnknownValue(value);
        }

        if (result instanceof UnknownValue) {
            LOGGER.debug("Unknown value: {} (type: {}, schema: {})", value, valueType, schema);
        }

        return result;
    }

    static Value doCreateValue(Object value, JSONObject schema) {
        Object val = (value == JSONObject.NULL ? null : value);
        if (schema != null) {
            String type = schema.optString("type");
            switch (type) {
            case "number":
                return new NumberValue(val);

            case "string":
            case "option":
            case "date":
            case "datetime":
                return new StringValue(val);

            case "any":
                if (val instanceof JSONObject) {
                    return new ObjectValue(val);
                } else if (val instanceof JSONArray) {
                    return new ArrayValue(val);
                } else {
                    return new StringValue(val);
                }

            case "array":
                if (val instanceof JSONObject) {
                    return new ObjectValue(val);
                } else {
                    String system = schema.optString("system");
                    if (system.equals("comment") || system.equals("worklog")) {
                        return new ObjectValue(val);
                    } else {
                        return new ArrayValue(val);
                    }
                }

            case "priority":
            case "user":
            case "issuetype":
            case "version":
            case "resolution":
                return new ObjectValue(val);
            }
        }
        if (val instanceof Number) {
            return new NumberValue(val);
        } else if (val instanceof String) {
            return new StringValue(val);
        } else if (val instanceof JSONArray) {
            return new ArrayValue(val);
        } else if (val instanceof JSONObject) {
            return new ObjectValue(val);
        }
        return new UnknownValue(val);
    }

    private static class UnknownValue extends AbstractValue {
        public UnknownValue(Object originalValue) {
            super(originalValue);
        }
    }

    private static class NumberValue extends AbstractValue {
        public NumberValue(Object originalValue) {
            super(originalValue);
        }

        @Override
        protected Object checkValue(Object val) {
            if (val instanceof Number) {
                return val;
            } else {
                throw new IllegalArgumentException("Not a number: " + val);
            }
        }
    }

    private static class StringValue extends AbstractValue {
        public StringValue(Object originalValue) {
            super(originalValue);
        }

        @Override
        protected Object checkValue(Object val) {
            if (val instanceof String) {
                return val;
            } else {
                throw new IllegalArgumentException("Not a string: " + val);
            }
        }
    }

    private static class ArrayValue extends AbstractValue {
        public ArrayValue(Object originalValue) {
            super(originalValue);
        }

        @Override
        protected Object checkValue(Object val) {
            if (val instanceof JSONArray) {
                return val;
            } else if (val instanceof String) {
                String str = (String) val;
                if (str.startsWith("[")) {
                    return new JSONArray(str);
                } else {
                    return new JSONArray().put(str);
                }
            } else {
                throw new IllegalArgumentException("Not a JSONArray: " + val);
            }
        }

        @Override
        protected Object cloneValue(Object val) {
            return new JSONArray(val.toString());
        }
    }

    private static class ObjectValue extends AbstractValue {
        public ObjectValue(Object originalValue) {
            super(originalValue);
        }

        @Override
        protected Object checkValue(Object val) {
            if (val instanceof JSONObject) {
                return val;
            } else if (val instanceof String) {
                return new JSONObject((String) val);
            } else {
                throw new IllegalArgumentException("Not a JSONObject: " + val);
            }
        }

        @Override
        protected Object cloneValue(Object val) {
            return new JSONObject(val.toString());
        }
    }

    private abstract static class AbstractValue implements Value {
        private final Object originalValue;

        private boolean modified;
        private Object value;

        public AbstractValue(Object originalValue) {
            this.originalValue = (originalValue == null ? null : doCheckValue(originalValue));
        }

        private Object doCheckValue(Object val) {
            try {
                return checkValue(val);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("Invalid value: " + val, e);
            }
        }

        protected Object checkValue(Object val) {
            return val;
        }

        @Override
        public final Object getValue() {
            Object val = (modified ? value : originalValue);
            return (val == null ? null : cloneValue(val));
        }

        protected Object cloneValue(Object val) {
            return val;
        }

        @Override
        public final void setValue(Object val) {
            this.value = (val == null ? null : doCheckValue(val));
            this.modified = true;
        }

        @Override
        public final boolean isModified() {
            return modified;
        }

        @Override
        public String toString() {
            Object val = (modified ? value : originalValue);
            return Objects.toString(val);
        }
    }
}
