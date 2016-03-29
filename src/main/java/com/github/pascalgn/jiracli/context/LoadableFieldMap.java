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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Value;

class LoadableFieldMap extends AbstractFieldMap {
    private final DefaultWebService webService;
    private Issue issue;

    private transient Collection<Field> fields;

    public LoadableFieldMap(DefaultWebService webService) {
        this.webService = webService;
    }

    public void setIssue(Issue issue) {
        this.issue = issue;
    }

    @Override
    public synchronized Collection<Field> getFields() {
        if (fields == null) {
            fields = new ArrayList<Field>();
            Map<String, String> fieldMapping = webService.getFieldMapping();
            JSONObject json = webService.getJson(issue.getKey());
            JSONObject jsonFields = json.getJSONObject("fields");
            for (String id : jsonFields.keySet()) {
                String name = fieldMapping.get(id);
                Value value = new ValueImpl(jsonFields.get(id));
                fields.add(new Field(issue, id, name, value));
            }
        }
        return fields;
    }

    private static class ValueImpl implements Value {
        private final Object value;

        public ValueImpl(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public void setValue(Object object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isModified() {
            return false;
        }
    }
}
