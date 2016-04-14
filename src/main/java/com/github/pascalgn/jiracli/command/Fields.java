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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.FieldList;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "fields", description = "Return the fields of the given issues")
class Fields implements Command {
    @Argument(names = { "-a", "--all" }, description = "return all fields, not only the loaded fields")
    private boolean all;

    @Override
    public FieldList execute(Context context, Data input) {
        Set<Hint> h = (all ? Collections.<Hint> singleton(IssueHint.allFields()) : Hint.none());
        return new FieldList(input.toIssueListOrFail().loadingSupplier(h, new Function<Issue, Collection<Field>>() {
            @Override
            public Collection<Field> apply(Issue issue, Set<Hint> hints) {
                if (all) {
                    return issue.getFieldMap().getFields();
                } else {
                    return issue.getFieldMap().getLoadedFields();
                }
            }
        }));
    }
}
