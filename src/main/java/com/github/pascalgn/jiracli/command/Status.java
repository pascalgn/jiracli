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

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Converter;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.Schema;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Function;

@CommandDescription(names = "status", description = "Get the current status of the given issues")
class Status implements Command {
    @Override
    public TextList execute(final Context context, Data input) {
        Schema schema = context.getWebService().getSchema();
        final Converter converter = schema.getConverter("status");
        IssueList issueList = input.toIssueListOrFail();
        return new TextList(issueList.convertingSupplier(new Function<Issue, Text>() {
            @Override
            public Text apply(Issue issue) {
                Object status = issue.getFieldMap().getFieldById("status").getValue().get();
                return new Text(converter.toString(status));
            }
        }));
    }
}
