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

import com.github.pascalgn.jiracli.command.CommandFactory;
import com.github.pascalgn.jiracli.command.CommandFactory.UsageException;
import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.parser.CommandReference;
import com.github.pascalgn.jiracli.util.Supplier;

class Shell {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

    private static final String PROMPT = "jiracli> ";

    private static final List<String> EXIT = Arrays.asList("exit", "quit", "q");

    private final Context context;
    private final CommandFactory commandFactory;

    public Shell(Context context) {
        this.context = context;
        this.commandFactory = CommandFactory.getInstance();
    }

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

            Data result = execute(line);

            if (result == null) {
                continue;
            }

            try {
                TextList textList = result.toTextList();
                if (textList != null) {
                    Text text;
                    while ((text = textList.next()) != null) {
                        console.println(text.getText());
                    }
                }
            } catch (RuntimeException e) {
                logException(console, e);
            }
        }
    }

    Data execute(String line) {
        Console console = context.getConsole();
        try {
            Pipeline.Builder pipelineBuilder = new Pipeline.Builder();

            List<CommandReference> commands = CommandReference.parseCommandReferences(line);
            for (CommandReference ref : commands) {
                pipelineBuilder.add(commandFactory.parseCommand(ref.getName(), ref.getArguments()));
            }

            Pipeline pipeline = pipelineBuilder.build();

            TextList input = new TextList(new ConsoleTextSupplier(console));

            return pipeline.execute(context, input);
        } catch (UsageException e) {
            console.println(e.getLocalizedMessage());
            return null;
        } catch (RuntimeException e) {
            logException(console, e);
            return null;
        }
    }

    private static void logException(Console console, Exception e) {
        LOGGER.debug("Error", e);
        if (e.getLocalizedMessage() == null) {
            console.println("error!");
        } else {
            console.println(e.getLocalizedMessage());
        }
    }

    private static class ConsoleTextSupplier implements Supplier<Text> {
        private final Console console;

        public ConsoleTextSupplier(Console console) {
            this.console = console;
        }

        @Override
        public Text get() {
            String raw = console.readLine();
            if (raw == null) {
                return null;
            }
            String line = raw.trim();
            return (line.isEmpty() ? null : new Text(line));
        }
    }
}
