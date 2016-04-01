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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ValueFactoryTest {
    @Test
    public void test1a() throws Exception {
        ValueFactory.createValue(1, null);
    }

    @Test
    public void test1b() throws Exception {
        ValueFactory.createValue(1.0, null);
    }

    @Test
    public void test1c() throws Exception {
        ValueFactory.createValue(1.0f, null);
    }

    @Test
    public void test1d() throws Exception {
        ValueFactory.createValue("Hallo", null);
    }

    @Test
    public void test2a() throws Exception {
        ValueFactory.createValue(new JSONArray("[1, 2]"), null);
    }

    @Test
    public void test2b() throws Exception {
        ValueFactory.createValue(new JSONObject("{a: 'b'}"), null);
    }

    @Test
    public void test3a() throws Exception {
        ValueFactory.createValue(new JSONArray("[1]"), new JSONObject("{type: 'array'}"));
    }

    @Test
    public void test3b() throws Exception {
        ValueFactory.createValue("1", new JSONObject("{type: 'array'}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test4() throws Exception {
        ValueFactory.doCreateValue(1, new JSONObject("{type: 'string'}"));
    }

    @Test
    public void test5a() throws Exception {
        ValueFactory.createValue(null, null);
    }

    @Test
    public void test5b() throws Exception {
        ValueFactory.createValue(JSONObject.NULL, null);
    }
}
