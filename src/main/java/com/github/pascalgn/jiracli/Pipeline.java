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
import com.github.pascalgn.jiracli.command.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Type;

class Pipeline<D extends Data<?>> {
    public static final class Builder<D extends Data<?>> {
        private final List<Command<?, ?, ?>> commands;

        public static Builder<None> newInstance() {
            return new Builder<None>();
        }

        public Builder() {
            this.commands = new ArrayList<>();
        }

        @SuppressWarnings("unchecked")
        public <R extends Data<?>> Builder<R> add(Command<?, D, R> command) {
            commands.add(command);
            return (Builder<R>) this;
        }

        public Pipeline<D> build() {
            return new Pipeline<>(commands);
        }
    }

    private final List<Command<?, ?, ?>> commands;

    private Pipeline(List<Command<?, ?, ?>> commands) {
        this.commands = commands;
    }

    @SuppressWarnings("unchecked")
    public D execute(Context context) {
        Data<?> result = None.getInstance();
        for (Command<?, ?, ?> command : commands) {
            Type inputType = command.getInputType();
            Data<?> input = result.convertTo(inputType);
            if (input == null) {
                throw new IllegalStateException("Cannot convert data to " + inputType + ": " + result);
            }
            result = ((Command<?, Data<?>, Data<?>>) command).execute(context, input);
        }
        return (D) result;
    }
}
