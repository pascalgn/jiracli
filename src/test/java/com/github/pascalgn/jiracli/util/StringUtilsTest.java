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
package com.github.pascalgn.jiracli.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StringUtilsTest {
    @Test
    public void test1() throws Exception {
        List<String> result = StringUtils.splitNewline("Hello\nWorld\r\nHello\nWorld");
        assertEquals(Arrays.asList("Hello", "World", "Hello", "World"), result);
    }

    @Test
    public void test2a() throws Exception {
        List<String> result = StringUtils.split("HelloAbcWorldAbcAbcHelloAbcWorld", "Abc");
        assertEquals(Arrays.asList("Hello", "World", "", "Hello", "World"), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2b() throws Exception {
        StringUtils.split("Hello", "");
    }

    @Test
    public void test2c() throws Exception {
        List<String> result = StringUtils.split("  a b  c ", " ");
        assertEquals(Arrays.asList("", "", "a", "b", "", "c", ""), result);
    }

    @Test
    public void test2d() throws Exception {
        List<String> result = StringUtils.split("a,b,", ",");
        assertEquals(Arrays.asList("a", "b", ""), result);
    }

    @Test
    public void test3a() throws Exception {
        assertEquals("abc", StringUtils.shorten("abc", 3));
    }

    @Test
    public void test3b() throws Exception {
        assertEquals("...", StringUtils.shorten("abcd", 3));
    }

    @Test
    public void test3c() throws Exception {
        assertEquals("abcd", StringUtils.shorten("abcd", 4));
    }

    @Test
    public void test3d() throws Exception {
        assertEquals("a...", StringUtils.shorten("abcde", 4));
    }

    @Test
    public void test4a() throws Exception {
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    public void test4b() throws Exception {
        assertEquals("Test", StringUtils.capitalize("test"));
    }

    @Test
    public void test4c() throws Exception {
        assertEquals("TEST", StringUtils.capitalize("TEST"));
    }

    @Test
    public void test4d() throws Exception {
        assertEquals("TeST", StringUtils.capitalize("teST"));
    }
}
