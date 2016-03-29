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
package com.github.pascalgn.jiracli.command;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.github.pascalgn.jiracli.model.Text;

public class SplitTest {
    @Test
    public void test1() throws Exception {
        Collection<Text> collection = new ArrayList<Text>();
        Split.splitNewline("Hello\nWorld\r\nHello\nWorld", collection);
        assertEquals(Arrays.asList("Hello", "World", "Hello", "World"), toStringList(collection));
    }

    @Test
    public void test2a() throws Exception {
        Collection<Text> collection = new ArrayList<Text>();
        Split.split("HelloAbcWorldAbcAbcHelloAbcWorld", "Abc", collection);
        assertEquals(Arrays.asList("Hello", "World", "", "Hello", "World"), toStringList(collection));
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2b() throws Exception {
        Split.split("Hello", "", Collections.<Text> emptyList());
    }

    @Test
    public void test2c() throws Exception {
        Collection<Text> collection = new ArrayList<Text>();
        Split.split("  a b  c ", " ", collection);
        assertEquals(Arrays.asList("", "", "a", "b", "", "c", ""), toStringList(collection));
    }

    @Test
    public void test2d() throws Exception {
        Collection<Text> collection = new ArrayList<Text>();
        Split.split("a,b,", ",", collection);
        assertEquals(Arrays.asList("a", "b", ""), toStringList(collection));
    }

    private static List<String> toStringList(Collection<? extends Text> collection) {
        List<String> result = new ArrayList<String>();
        for (Text text : collection) {
            result.add(text.getText());
        }
        return result;
    }
}
