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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.github.pascalgn.jiracli.model.AbstractFieldMap;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Value;

public class IssueFactory {
    public static Issue create(String key, Field... fields) {
        return new Issue(key, new FieldMapImpl(Arrays.asList(fields)));
    }

    /**
     * @param key The issue key, for example <code>ISSUE-1234</code>
     * @param fields Key/value pairs, for example <code>summary, A title, description, A description</code>
     */
    public static Issue create(String key, Object... fields) {
        if (fields.length % 2 != 0) {
            throw new IllegalArgumentException("Invalid key/value pairs: " + Arrays.toString(fields));
        }
        List<Field> fieldList = new ArrayList<Field>();
        Issue issue = new Issue(key, new FieldMapImpl(fieldList));
        for (int i = 0; i < fields.length; i += 2) {
            String id = (String) fields[i];
            Object value = fields[i + 1];
            fieldList.add(new Field(issue, id, new Value(value)));
        }
        return issue;
    }

    private static class FieldMapImpl extends AbstractFieldMap {
        private final Collection<Field> fields;

        public FieldMapImpl(Collection<Field> fields) {
            this.fields = fields;
        }

        @Override
        public Collection<Field> getFields() {
            return fields;
        }

        @Override
        public void addField(Field field) {
            fields.add(field);
        }
    }
}
