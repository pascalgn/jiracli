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

import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldMap;

abstract class AbstractFieldMap implements FieldMap {
    @Override
    public Field getFieldById(String id) {
        for (Field field : getFields()) {
            if (field.getId().equals(id)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public Field getFieldByName(String name) {
        String lower = name.toLowerCase();
        for (Field field : getFields()) {
            if (field.getName().toLowerCase().equals(lower)) {
                return field;
            }
        }
        return null;
    }
}
