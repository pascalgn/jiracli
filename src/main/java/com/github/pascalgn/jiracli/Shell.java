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
import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.ConversionException;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.parser.CommandReference;

class Shell {
    private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

    private static final String PROMPT = "jiracli> ";

    private static final List<String> EXIT = Arrays.asList("exit", "quit", "q");

    private final Context context;

    public Shell(Context context) {
        this.context = context;
    }

    public void start() {
        Console console = context.getConsole();
        CommandFactory commandFactory = CommandFactory.getInstance();
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

            try {
                Pipeline.Builder<Data<?>> pipelineBuilder = new Pipeline.Builder<>();

                List<CommandReference> commands = CommandReference.parseCommandReferences(line);
                for (CommandReference ref : commands) {
                    pipelineBuilder.add(commandFactory.parseCommand(ref.getName(), ref.getArguments()));
                }

                Pipeline<Data<?>> pipeline = pipelineBuilder.build();
                pipeline.execute(context);
            } catch (ConversionException e) {
                LOGGER.trace("Conversion error", e);
                console.println("invalid input!");
            } catch (RuntimeException e) {
                if (e.getLocalizedMessage() == null) {
                    LOGGER.info("Error", e);
                    console.println("error!");
                } else {
                    LOGGER.trace("Error", e);
                    console.println(e.getLocalizedMessage());
                }
            }
        }
    }
}
