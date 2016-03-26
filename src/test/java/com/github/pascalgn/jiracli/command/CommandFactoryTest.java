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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.Test;

public class CommandFactoryTest {
    @Test
    public void testGet() throws Exception {
        checkUsage("usage: get [-h] [--] <issue>...", "get");
    }

    @Test
    public void testReadExcel() throws Exception {
        checkUsage("usage: readexcel [-c <col>] [-h] [--] <file>", "re");
    }

    @Test
    public void testPrint() throws Exception {
        checkUsage("usage: print [-n] [-h] [--] [<format>]", "p");
    }

    private void checkUsage(String usage, String command) {
        CommandFactory commandFactory = CommandFactory.getInstance();
        String message = null;
        try {
            commandFactory.parseCommand(command, Arrays.asList("--INVALID_ARG"));
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        }
        assertEquals(usage, message);
    }

    @Test
    public void testParseReadExcel() throws Exception {
        Command readExcel = parse("re", "-c", "ABC", "-");
        assertEquals("ABC", getFieldValue(readExcel, "column"));
    }

    @Test
    public void testParseFilter() throws Exception {
        Command filter = parse("filter", "field1", "value1");
        assertEquals("field1", getFieldValue(filter, "field"));
        assertEquals("value1", ((Pattern) getFieldValue(filter, "pattern")).pattern());
    }

    @Test
    public void testParsePrint() throws Exception {
        Command print = parse("print", "-n");
        assertEquals(true, getFieldValue(print, "noNewline"));
    }

    private Command parse(String name, String... args) {
        CommandFactory commandFactory = CommandFactory.getInstance();
        return commandFactory.parseCommand(name, Arrays.asList(args));
    }

    private static Object getFieldValue(Object instance, String name) {
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(instance);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
