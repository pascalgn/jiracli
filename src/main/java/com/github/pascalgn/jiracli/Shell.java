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
package com.github.pascalgn.jiracli;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Base64Factory;
import com.github.pascalgn.jiracli.command.Command;
import com.github.pascalgn.jiracli.command.CommandFactory;
import com.github.pascalgn.jiracli.command.Context;
import com.github.pascalgn.jiracli.command.PrintFactory;
import com.github.pascalgn.jiracli.command.ReadExcelFactory;
import com.github.pascalgn.jiracli.command.ReadFactory;
import com.github.pascalgn.jiracli.console.Console;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.parser.CommandReference;

class Shell {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

    private static final String PROMPT = "jiracli> ";

    private static final List<String> EXIT = Arrays.asList("exit", "quit", "q");

    private static final List<String> HELP = Arrays.asList("help", "h", "?");

    private static final List<CommandFactory> COMMAND_FACTORIES = Arrays.asList(new ReadFactory(),
            new ReadExcelFactory(), new PrintFactory(), new Base64Factory());

    public static CommandFactory getCommandFactory(String commandName) {
        for (CommandFactory commandFactory : COMMAND_FACTORIES) {
            if (commandFactory.getName().equals(commandName) || commandFactory.getAliases().contains(commandName)) {
                return commandFactory;
            }
        }
        throw new IllegalArgumentException("Unknown command: " + commandName);
    }

    private final Context context;

    public Shell(Context context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    public void start() {
        Console console = context.getConsole();
        while (true) {
            console.print(PROMPT);

            String raw = console.readLine();
            if (raw == null) {
                break;
            }

            String line = raw.trim();

            if (EXIT.contains(line)) {
                break;
            } else if (line.isEmpty()) {
                continue;
            }

            if (HELP.contains(line)) {
                for (CommandFactory commandFactory : COMMAND_FACTORIES) {
                    StringBuilder str = new StringBuilder();
                    str.append(commandFactory.getName());
                    str.append("  ");
                    str.append(commandFactory.getDescription());
                    str.append("\n");
                    str.append(repeat(" ", commandFactory.getName().length()));
                    str.append("  (Aliases: ");
                    str.append(join(commandFactory.getAliases(), ", "));
                    str.append(")");
                    console.println(str.toString());
                }
                continue;
            }

            try {
                Pipeline.Builder<Data<?>> pipelineBuilder = new Pipeline.Builder<>();

                List<CommandReference> commands = CommandReference.parseCommandReferences(line);
                for (CommandReference ref : commands) {
                    CommandFactory commandFactory = getCommandFactory(ref.getName());
                    Command<?, ?, ?> command = commandFactory.createCommand(ref.getArguments());
                    pipelineBuilder.add((Command<?, Data<?>, Data<?>>) command);
                }

                Pipeline<Data<?>> pipeline = pipelineBuilder.build();
                pipeline.execute(context);
            } catch (RuntimeException e) {
                LOGGER.trace("Error", e);
                console.println("Error: " + e.getLocalizedMessage());
            }
        }
    }

    private static String join(List<String> list, String sep) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                result.append(sep);
            }
            result.append(list.get(i));
        }
        return result.toString();
    }

    private static String repeat(String str, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }
}
