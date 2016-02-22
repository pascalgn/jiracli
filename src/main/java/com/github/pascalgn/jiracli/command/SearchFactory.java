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
package com.github.pascalgn.jiracli.command;

import java.util.Arrays;
import java.util.List;

public class SearchFactory implements CommandFactory {
    @Override
    public String getName() {
        return "search";
    }

    @Override
    public String getDescription() {
        return "Search for issues via JQL";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("s");
    }

    @Override
    public Search createCommand(List<String> arguments) {
        if (arguments.size() == 1) {
            return new Search(arguments.get(0));
        } else {
            throw new IllegalArgumentException("Invalid arguments: " + arguments);
        }
    }
}
