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

import java.io.StringReader;

import org.junit.Test;

public class IOUtilsTest {
    @Test
    public void test1a() throws Exception {
        assertEquals("", IOUtils.toString(new StringReader(""), 3));
    }

    @Test
    public void test1b() throws Exception {
        assertEquals("", IOUtils.toString(new StringReader("123"), 0));
    }

    @Test
    public void test1c() throws Exception {
        assertEquals("12", IOUtils.toString(new StringReader("123"), 2));
    }

    @Test
    public void test1d() throws Exception {
        int bufferSize = IOUtils.BUFFER_SIZE;
        String str = StringUtils.repeat("x", bufferSize);
        assertEquals(str + "123", IOUtils.toString(new StringReader(str + "12345"), bufferSize + 3));
    }
}
