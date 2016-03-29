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
package com.github.pascalgn.jiracli.testutil;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.pascalgn.jiracli.context.DefaultFieldMap;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Value;

public class IssueFactory {
    public static Issue create(String key, Field... fields) {
        return new Issue(key, createURI(key), new DefaultFieldMap(Arrays.asList(fields)));
    }

    public static Issue create(String key, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid key/value pairs: " + Arrays.toString(fields));
        }
        List<Field> fieldList = new ArrayList<Field>();
        Issue issue = new Issue(key, createURI(key), new DefaultFieldMap(fieldList));
        for (int i = 0; i < fields.length; i += 2) {
            String id = (String) fields[i];
            Object value = fields[i + 1];
            fieldList.add(new Field(issue, id, id, new ValueImpl(value)));
        }
        return issue;
    }

    private static URI createURI(String key) {
        return URI.create("issue://" + key);
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
