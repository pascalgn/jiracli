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
package com.github.pascalgn.jiracli.parser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class CommandReferenceTest {
    @Test
    public void testParse1() throws Exception {
        parse("cmd1", "cmd:cmd1;");
    }

    @Test
    public void testParse2a() throws Exception {
        parse("cmd1 arg", "cmd:cmd1;arg:arg;");
    }

    @Test
    public void testParse2b() throws Exception {
        parse("cmd1 arg1 arg2", "cmd:cmd1;arg:arg1;arg:arg2;");
    }

    @Test
    public void testParse3a() throws Exception {
        parse("cmd1 | cmd2", "cmd:cmd1;cmd:cmd2;");
    }

    @Test
    public void testParse3b() throws Exception {
        parse("cmd1 arg1|cmd2", "cmd:cmd1;arg:arg1;cmd:cmd2;");
    }

    @Test
    public void testParse4() throws Exception {
        parse("c key=value | d", "cmd:c;arg:key=value;cmd:d;");
    }

    @Test
    public void testParse5a() throws Exception {
        parse("cmd1 'arg with spaces'", "cmd:cmd1;arg:arg with spaces;");
    }

    @Test
    public void testParse5b() throws Exception {
        parse("cmd1 'arg1' 'arg2'", "cmd:cmd1;arg:arg1;arg:arg2;");
    }
    @Test
    public void testParse5c() throws Exception {
        parse("cmd1 \"arg with spaces\"", "cmd:cmd1;arg:arg with spaces;");
    }

    @Test
    public void testParse5d() throws Exception {
        parse("cmd1 \"arg1\" \"arg2\"", "cmd:cmd1;arg:arg1;arg:arg2;");
    }

    private void parse(String input, String expected) {
        List<CommandReference> parsed = CommandReference.parseCommandReferences(input);

        StringBuilder str = new StringBuilder();
        for (CommandReference cmd : parsed) {
            str.append("cmd:");
            str.append(cmd.getName());
            str.append(";");
            for (String arg : cmd.getArguments()) {
                str.append("arg:");
                str.append(arg);
                str.append(";");
            }
        }

        assertEquals(expected, str.toString());
    }
}
