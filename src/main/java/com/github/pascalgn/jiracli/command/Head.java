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

import java.util.Set;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Filter;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "head", description = "Return only the first items of a list")
class Head implements Command {
    @Argument(names = "-n", parameters = Parameters.ONE, variable = "<count>", description = "Number of items")
    private int count = 10;

    @Override
    public Data execute(Context context, Data input) {
        final int[] index = new int[1];
        return input.toListOrFail(new Filter<Data>() {
            @Override
            public Data get(Supplier<Data> supplier, Set<Hint> hints) {
                if (index[0] < count) {
                    index[0]++;
                    return supplier.get(hints);
                } else {
                    return null;
                }
            }
        });
    }
}
