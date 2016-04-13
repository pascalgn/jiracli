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

import com.github.pascalgn.jiracli.model.Converter;

final class FieldInfo {
    private final String name;
    private final Converter converter;

    public FieldInfo(String name, Converter converter) {
        this.name = Objects.requireNonNull(name);
        this.converter = Objects.requireNonNull(converter);
    }

    public String getName() {
        return name;
    }

    public Converter getConverter() {
        return converter;
    }
}
