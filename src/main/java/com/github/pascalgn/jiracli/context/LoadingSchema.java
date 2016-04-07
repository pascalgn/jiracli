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

import java.util.Map;
import java.util.Objects;

import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Schema;

abstract class LoadingSchema implements Schema {
    private Map<String, FieldInfo> map;

    private synchronized FieldInfo getFieldInfo(String fieldId) {
        if (map == null) {
            map = loadMap();
        }
        FieldInfo fieldInfo = map.get(fieldId);
        if (fieldInfo == null) {
            throw new IllegalArgumentException("Unknown field: " + fieldId);
        }
        return fieldInfo;
    }

    protected abstract Map<String, FieldInfo> loadMap();

    @Override
    public String getName(Field field) {
        return getFieldInfo(field.getId()).name;
    }

    @Override
    public Converter getConverter(Field field) {
        return getFieldInfo(field.getId()).converter;
    }

    public Converter getConverter(String fieldId) {
        return getFieldInfo(fieldId).converter;
    }

    public static final class FieldInfo {
        private final String name;
        private final Converter converter;

        public FieldInfo(String name, Converter converter) {
            this.name = Objects.requireNonNull(name);
            this.converter = Objects.requireNonNull(converter);
        }
    }
}
