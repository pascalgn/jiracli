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

import java.io.IOException;
import java.util.ArrayList;
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

class Shell {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

    private static final String PROMPT = "jiracli> ";
    private static final String EXIT = "exit";
    private static final String QUIT = "quit";

    private static final List<CommandFactory> COMMAND_FACTORIES = Arrays.asList(new ReadFactory(),
            new ReadExcelFactory(), new PrintFactory(), new Base64Factory());

    public static CommandFactory getCommandFactory(String commandName) {
        for (CommandFactory commandFactory : COMMAND_FACTORIES) {
            if (commandFactory.getName().equals(commandName)) {
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
    public void start() throws IOException {
        Console console = context.getConsole();
        while (true) {
            console.print(PROMPT);

            String raw = console.readLine();
            if (raw == null) {
                break;
            }

            String line = raw.trim();

            if (line.equals(EXIT) || line.equals(QUIT)) {
                break;
            } else if (line.isEmpty()) {
                continue;
            }

            try {
                Pipeline.Builder<Data<?>> pipelineBuilder = new Pipeline.Builder<>();
                String[] commandNames = line.trim().split("\\s*\\|\\s*");
                for (String commandName : commandNames) {
                    String[] arr = commandName.split(" ");
                    CommandFactory commandFactory = getCommandFactory(arr[0]);
                    Command<?, ?, ?> command = commandFactory.createCommand(toList(arr, 1, arr.length - 1));
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

    private static <T> List<T> toList(T[] arr, int offset, int length) {
        List<T> result = new ArrayList<T>(length);
        for (int i = offset; i < offset + length; i++) {
            result.add(arr[i]);
        }
        return result;
    }
}
