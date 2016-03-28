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
