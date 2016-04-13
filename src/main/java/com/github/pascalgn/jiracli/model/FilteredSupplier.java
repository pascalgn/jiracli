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
package com.github.pascalgn.jiracli.model;

import java.util.Set;

import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

class FilteredSupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final Filter<T> filter;

    public FilteredSupplier(Supplier<T> supplier, Filter<T> filter) {
        this.supplier = supplier;
        this.filter = filter;
    }

    @Override
    public T get(Set<Hint> hints) {
        return filter.get(supplier, hints);
    }
}
