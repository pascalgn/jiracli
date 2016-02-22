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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.Test;

import com.github.pascalgn.jiracli.parser.CommandLineParser.ArgumentContext;
import com.github.pascalgn.jiracli.parser.CommandLineParser.CommandLineContext;
import com.github.pascalgn.jiracli.parser.CommandLineParser.CommandNameContext;

public class CommandLineParserTest {
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

    private void parse(String input, String expected) {
        CommandLineLexer lexer = new CommandLineLexer(new ANTLRInputStream(input));
        CommandLineParser parser = new CommandLineParser(new CommonTokenStream(lexer));
        CommandLineContext commandLine = parser.commandLine();

        final StringBuilder str = new StringBuilder();

        class Listener extends CommandLineParserBaseListener {
            @Override
            public void enterCommandName(CommandNameContext ctx) {
                str.append("cmd:" + ctx.getText() + ";");
            }

            @Override
            public void enterArgument(ArgumentContext ctx) {
                str.append("arg:" + ctx.getText() + ";");
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
                throw new IllegalStateException(
                        "Unexpected content: " + node.getText() + " (" + node.getSymbol().getType() + ")");
            }
        }

        ParseTreeWalker.DEFAULT.walk(new Listener(), commandLine);

        assertEquals(expected, str.toString());
    }
}
