/*
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

import java.util.Objects;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Configuration;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;

@CommandDescription(names = "config", description = "Change configuration values or display the current configuration")
class Config implements Command {
    @Argument(names = "-b", parameters = Parameters.ONE, variable = "<url>",
            description = "the base URL of the Jira service")
    private String baseUrl;

    @Argument(names = "-u", parameters = Parameters.ONE, variable = "<user>",
            description = "the default username used for authentication")
    private String username;

    @Override
    public Data execute(Context context, Data input) {
        Configuration configuration = context.getConfiguration();
        if (baseUrl != null) {
            configuration.setBaseUrl(baseUrl);
        }
        if (username != null) {
            configuration.setUsername(username);
        }
        String url = Objects.toString(configuration.getBaseUrl(), "");
        String user = Objects.toString(configuration.getUsername(), "");
        return new TextList(new Text("Base URL: " + url), new Text("Username: " + user));
    }
}
