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

import java.util.Collection;

public interface FieldMap {
    /**
     * @return All fields, never null
     */
    Collection<Field> getFields();

    /**
     * @return All currently loaded fields, never null
     */
    Collection<Field> getLoadedFields();

    /**
     * Add the field if it does not already exist in this map
     */
    void addField(Field field);

    /**
     * @return The field with the given id (or name) or <code>null</code>
     */
    Field getField(String idOrName, Schema schema);

    /**
     * @return The field with the given ID or <code>null</code>
     */
    Field getFieldById(String id);

    /**
     * @return The field with the given name or <code>null</code>
     */
    Field getFieldByName(String name, Schema schema);
}
