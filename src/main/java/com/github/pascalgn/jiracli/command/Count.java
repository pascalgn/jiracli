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
import java.util.Iterator;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.IssueHint;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.util.Hint;

@CommandDescription(names = "count", description = "Count the given items")
class Count implements Command {
    @Override
    public Text execute(final Context context, Data input) {
        int count = 0;
        Iterator<Data> it = input.toIterator(Collections.<Hint> singleton(IssueHint.count()));
        while (it.hasNext()) {
            it.next();
            ++count;
        }
        return new Text(Integer.toString(count));
    }
}
