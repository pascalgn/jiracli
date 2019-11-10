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
package com.github.pascalgn.jiracli.context;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.github.pascalgn.jiracli.context.WebService.Request;

public class DefaultRequest implements Request {
    private final boolean allFields;
    private final Collection<String> fields;
    private final Collection<String> expand;

    public DefaultRequest() {
        this(false);
    }

    public DefaultRequest(boolean allFields) {
        this(allFields, Collections.<String> emptyList(), Collections.<String> emptyList());
    }

    public DefaultRequest(String... fields) {
        this(false, Arrays.asList(fields), Collections.<String> emptyList());
    }

    public DefaultRequest(boolean allFields, Collection<String> fields, Collection<String> expand) {
        this.allFields = allFields;
        this.fields = fields;
        this.expand = expand;
    }

    @Override
    public boolean getAllFields() {
        return allFields;
    }

    @Override
    public Collection<String> getFields() {
        return fields;
    }

    @Override
    public Collection<String> getExpand() {
        return expand;
    }

    @Override
    public String toString() {
        return "DefaultRequest[allFields=" + allFields + ", fields=" + fields + ", expand=" + expand + "]";
    }
}
