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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.pascalgn.jiracli.model.AbstractFieldMap;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Supplier;

class LoadingFieldMap extends AbstractFieldMap {
    private final Map<String, Field> fields;
    private final AtomicBoolean loaded;

    private Supplier<List<Field>> supplier;

    public LoadingFieldMap() {
        this.loaded = new AtomicBoolean();
        this.fields = new HashMap<>();
    }

    void setSupplier(Supplier<List<Field>> supplier) {
        if (this.supplier != null) {
            throw new IllegalStateException();
        }
        this.supplier = supplier;
    }

    @Override
    public synchronized void addField(Field field) {
        String id = field.getId();
        if (fields.containsKey(id)) {
            throw new IllegalStateException("Field already exists: " + id);
        }
        fields.put(id, field);
    }

    @Override
    public Collection<Field> getFields() {
        return getFields(true);
    }

    public Collection<Field> getLoadedFields() {
        return getFields(false);
    }

    @Override
    public Field getFieldById(String id) {
        Field field = getFieldById(getFields(false), id);
        if (field == null) {
            field = getFieldById(getFields(true), id);
        }
        return field;
    }

    @Override
    public Field getFieldByName(String name, Function<Field, String> function) {
        Field field = getFieldByName(getFields(false), name, function);
        if (field == null) {
            field = getFieldByName(getFields(true), name, function);
        }
        return field;
    }

    private synchronized Collection<Field> getFields(boolean loadAll) {
        if (loadAll && loaded.compareAndSet(false, true)) {
            List<Field> all = supplier.get();
            for (Field field : all) {
                String id = field.getId();
                if (!fields.containsKey(id)) {
                    fields.put(id, field);
                }
            }
        }
        return fields.values();
    }
}
