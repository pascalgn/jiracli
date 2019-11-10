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

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.User;

@CommandDescription(names = { "authenticate", "auth" }, description = "Trigger an authentication request")
class Authenticate implements Command {
    @Argument(names = { "-i", "--id" }, description = "return the user ID")
    private boolean id;

    @Override
    public Data execute(Context context, Data input) {
        User user = context.getWebService().authenticate();
        return id ? new Text(user.getId()) : None.getInstance();
    }
}
