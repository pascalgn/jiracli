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

import java.util.Collections;
import java.util.List;

public class FilterFactory implements CommandFactory {
    @Override
    public String getName() {
        return "filter";
    }

    @Override
    public String getDescription() {
        return "Filter issues by the given field value";
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public Filter createCommand(List<String> arguments) {
        if (arguments.size() == 2) {
            return new Filter(arguments.get(0), arguments.get(1));
        } else {
            throw new IllegalArgumentException("Invalid arguments: " + arguments);
        }
    }
}
