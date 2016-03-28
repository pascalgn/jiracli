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

import com.github.pascalgn.jiracli.util.Supplier;

abstract class ListSupplier<D extends Data, R> implements Supplier<R> {
    private final List<D> list;

    public ListSupplier(List<D> list) {
        this.list = list;
    }

    @Override
    public R get() {
        D next = list.next();
        return (next == null ? null : convert(next));
    }

    protected abstract R convert(D item);
}
