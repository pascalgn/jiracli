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
        CommandLineContext commandLine = parser.commandLine();

        final List<CommandReference> commands = new ArrayList<CommandReference>();

        final StringBuilder name = new StringBuilder();
        final List<String> arguments = new ArrayList<String>();

        class Listener extends CommandLineParserBaseListener {
            @Override
            public void enterCommandName(CommandNameContext ctx) {
                if (name.length() == 0) {
                    name.append(ctx.getText());
                } else {
                    List<String> args = new ArrayList<String>(arguments);
                    CommandReference command = new CommandReference(name.toString(), args);
                    commands.add(command);
                    name.setLength(0);
                    name.append(ctx.getText());
                    arguments.clear();
                }
            }

            @Override
            public void enterArgument(ArgumentContext ctx) {
                arguments.add(ctx.getText());
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
