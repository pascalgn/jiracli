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

import java.util.ArrayList;
import java.util.List;

import com.github.pascalgn.jiracli.command.Command;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;

class Pipeline {
    public static final class Builder {
        private final List<Command> commands;

        public Builder() {
            this.commands = new ArrayList<>();
        }

        public Builder add(Command command) {
            commands.add(command);
            return this;
        }

        public Pipeline build() {
            return new Pipeline(commands);
        }
    }

    private final List<Command> commands;

    private Pipeline(List<Command> commands) {
        if (commands.isEmpty()) {
            throw new IllegalArgumentException("No commands given!");
        }
        this.commands = new ArrayList<>(commands);
    }

    public Data execute(Context context, Data input) {
        Data result = input;
        for (Command command : commands) {
            result = command.execute(context, result);
        }
        return result;
    }
}
