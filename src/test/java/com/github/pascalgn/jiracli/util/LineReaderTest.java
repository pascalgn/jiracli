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

import java.util.Arrays;

import org.junit.Test;

public class LineReaderTest {
    @Test
    public void test1a() throws Exception {
        assertEquals(Arrays.asList(), LineReader.readLines(""));
    }

    @Test
    public void test1b() throws Exception {
        assertEquals(Arrays.asList("\n"), LineReader.readLines("\n"));
    }

    @Test
    public void test1c() throws Exception {
        assertEquals(Arrays.asList("\r\n"), LineReader.readLines("\r\n"));
    }

    @Test
    public void test1d() throws Exception {
        assertEquals(Arrays.asList("Hello\n", "World"), LineReader.readLines("Hello\nWorld"));
    }

    @Test
    public void test1e() throws Exception {
        assertEquals(Arrays.asList("Hello\r\n", "World\r\n"), LineReader.readLines("Hello\r\nWorld\r\n"));
    }
}
