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
package com.github.pascalgn.jiracli.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReflectionUtilsTest {
    @Test
    public void test1() throws Exception {
        class Test {
            public int getId() {
                return 123;
            }

            public String getName() {
                return "some name";
            }
        }
        Test test = new Test();
        assertEquals(test.getId(), ReflectionUtils.getValue(test, "id"));
        assertEquals(test.getName(), ReflectionUtils.getValue(test, "name"));
    }
}
