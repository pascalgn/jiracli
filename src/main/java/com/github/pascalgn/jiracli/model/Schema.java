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

import java.util.Set;

public interface Schema {
    /**
     * @return A list of field IDs, never null
     */
    Set<String> getFields();

    /**
     * @param field Field name or ID
     */
    String getId(String field);

    /**
     * @param field Field ID
     */
    String getName(String field);

    /**
     * @param field Field ID
     */
    Converter getConverter(String field);
}
