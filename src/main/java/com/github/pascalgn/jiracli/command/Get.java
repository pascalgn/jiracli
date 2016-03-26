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

import java.util.List;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "get", description = "Return the given issues")
class Get implements Command {
    @Argument(parameters = Parameters.ONE_OR_MORE, variable = "<issue>", description = "the issues")
    private List<String> issues;

    @Override
    public IssueList execute(Context context, Data<?> input) {
        return new IssueList(new Supplier<Issue>() {
            @Override
            public Issue get() {
                if (issues.isEmpty()) {
                    return null;
                } else {
                    return Issue.valueOf(issues.remove(0));
                }
            }
        });
    }
}
