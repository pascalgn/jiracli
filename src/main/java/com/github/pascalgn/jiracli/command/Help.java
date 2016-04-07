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

import java.util.ArrayList;
import java.util.List;

import com.github.pascalgn.jiracli.command.CommandFactory.CommandDescriptor;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.StringUtils;

@CommandDescription(names = { "help", "h", "?" }, description = "Show a list of available commands")
class Help implements Command {
    @Override
    public Data execute(Context context, Data input) {
        List<Text> textList = new ArrayList<Text>();
        CommandFactory commandFactory = CommandFactory.getInstance();
        for (CommandDescriptor commandDescriptor : commandFactory.getCommandDescriptors()) {
            StringBuilder str = new StringBuilder();
            str.append(StringUtils.join(commandDescriptor.getNames(), ", "));
            str.append(System.lineSeparator());
            str.append("    ");
            str.append(commandDescriptor.getDescription());
            textList.add(new Text(str.toString()));
        }
        return new TextList(textList.iterator());
    }
}
