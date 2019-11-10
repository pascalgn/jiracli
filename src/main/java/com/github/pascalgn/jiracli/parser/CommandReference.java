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
package com.github.pascalgn.jiracli.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.github.pascalgn.jiracli.parser.CommandLineParser.ArgumentContext;
import com.github.pascalgn.jiracli.parser.CommandLineParser.CommandLineContext;
import com.github.pascalgn.jiracli.parser.CommandLineParser.CommandNameContext;

public final class CommandReference {
    public static List<CommandReference> parseCommandReferences(String str) {
        CommandLineLexer lexer = new CommandLineLexer(new ANTLRInputStream(str));
        CommandLineParser parser = new CommandLineParser(new CommonTokenStream(lexer));

        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        CommandLineContext commandLine = parser.commandLine();

        final List<CommandReference> commands = new ArrayList<>();

        final StringBuilder name = new StringBuilder();
        final List<String> arguments = new ArrayList<>();

        class Listener extends CommandLineParserBaseListener {
            @Override
            public void enterCommandName(CommandNameContext ctx) {
                if (name.length() == 0) {
                    name.append(ctx.getText());
                } else {
                    List<String> args = new ArrayList<>(arguments);
                    CommandReference command = new CommandReference(name.toString(), args);
                    commands.add(command);
                    name.setLength(0);
                    name.append(ctx.getText());
                    arguments.clear();
                }
            }

            @Override
            public void enterArgument(ArgumentContext ctx) {
                String arg = ctx.getText();
                if (arg.startsWith("'") && arg.endsWith("'")) {
                    arguments.add(unescape(arg.substring(1, arg.length() - 1)));
                } else if (arg.startsWith("\"") && arg.endsWith("\"")) {
                    arguments.add(unescape(arg.substring(1, arg.length() - 1)));
                } else {
                    arguments.add(unescape(arg));
                }
            }

            @Override
            public void visitErrorNode(ErrorNode node) {
                throw new IllegalStateException("Unexpected content: " + node.getText());
            }
        }

        ParseTreeWalker.DEFAULT.walk(new Listener(), commandLine);

        if (name.length() > 0) {
            CommandReference command = new CommandReference(name.toString(), arguments);
            commands.add(command);
        }

        return commands;
    }

    private static String unescape(String str) {
        StringBuilder s = new StringBuilder();
        boolean escaped = false;
        for (int p = 0; p < str.length(); p++) {
            char c = str.charAt(p);
            if (c == '\\') {
                if (escaped) {
                    escaped = false;
                    s.append(c);
                } else {
                    escaped = true;
                }
            } else if (escaped) {
                escaped = false;
                if (c == 'n') {
                    s.append('\n');
                } else if (c == 'r') {
                    s.append('\r');
                } else if (c == 't') {
                    s.append('\t');
                } else if (c == '\'') {
                    s.append('\'');
                } else if (c == '"') {
                    s.append('"');
                } else {
                    throw new IllegalArgumentException("Invalid escape sequence: \\" + c);
                }
            } else {
                s.append(c);
            }
        }
        return s.toString();
    }

    private final String name;
    private final List<String> arguments;

    private CommandReference(String name, List<String> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "CommandReference[name=" + name + ", arguments=" + arguments + "]";
    }
}
