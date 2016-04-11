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
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.parser.CommandReference;
import com.github.pascalgn.jiracli.util.RuntimeInterruptedException;

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

        console.println(Constants.getTitle());

        String baseUrl = context.getConfiguration().getBaseUrl();
        if (baseUrl != null) {
            console.println("Base URL: " + baseUrl);
            console.println("You can change the base URL with the 'config' command");
        }

        final Thread shell = Thread.currentThread();
        console.onInterrupt(new Runnable() {
            @Override
            public void run() {
                LOGGER.trace("Interrupting thread: {}", shell.getName());
                try {
                    shell.interrupt();
                } catch (RuntimeException e) {
                    LOGGER.debug("Exception while interrupting {}", shell.getName(), e);
                }
            }
        });

        while (true) {
            console.print(PROMPT);

            String raw;
            try {
                raw = console.readCommand();
            } catch (RuntimeInterruptedException e) {
                console.println("");
                continue;
            }

            if (raw == null) {
                break;
            }

            String line = raw.trim();

            if (EXIT.contains(line)) {
                break;
            } else if (line.isEmpty()) {
                continue;
            }

            execute(line);
        }
    }

    void execute(String line) {
        Console console = context.getConsole();

        Data result;
        try {
            Pipeline.Builder pipelineBuilder = new Pipeline.Builder();

            List<CommandReference> commands = CommandReference.parseCommandReferences(line);
            for (CommandReference ref : commands) {
                pipelineBuilder.add(commandFactory.parseCommand(ref.getName(), ref.getArguments()));
            }

            Pipeline pipeline = pipelineBuilder.build();
            result = pipeline.execute(context, None.getInstance());
        } catch (UsageException e) {
            console.println(e.getLocalizedMessage());
            return;
        } catch (RuntimeInterruptedException e) {
            console.println("interrupted");
            return;
        } catch (RuntimeException e) {
            logException(console, e);
            return;
        }

        if (result == null) {
            return;
        }

        try {
            TextList textList = result.toTextList();
            if (textList != null) {
                Text text;
                while ((text = textList.next()) != null) {
                    console.println(text.getText());
                }
            }
        } catch (RuntimeInterruptedException e) {
            console.println("interrupted");
            return;
        } catch (RuntimeException e) {
            logException(console, e);
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
}
