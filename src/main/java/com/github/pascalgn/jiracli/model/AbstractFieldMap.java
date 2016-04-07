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
package com.github.pascalgn.jiracli.model;

import java.util.Collection;

import com.github.pascalgn.jiracli.util.Function;

public abstract class AbstractFieldMap implements FieldMap {
    @Override
    public Field getFieldById(String id) {
        return getFieldById(getFields(), id);
    }

    protected static Field getFieldById(Collection<Field> fields, String id) {
        for (Field field : fields) {
            if (field.getId().equals(id)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public Field getFieldByName(String name, Function<Field, String> function) {
        return getFieldByName(getFields(), name, function);
    }

    protected static Field getFieldByName(Collection<Field> fields, String name, Function<Field, String> function) {
        String lower = name.toLowerCase();
        for (Field field : fields) {
            String fieldName = function.apply(field);
            if (fieldName.toLowerCase().equals(lower)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return 31 + getFields().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof FieldMap)) {
            return false;
        }
        FieldMap other = (FieldMap) obj;
        return getFields().equals(other.getFields());
    }
}
