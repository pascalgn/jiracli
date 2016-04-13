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
package com.github.pascalgn.jiracli.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Set;

public abstract class CollectingSupplier<T> implements Supplier<T> {
    private final Deque<T> items;

    public CollectingSupplier() {
        this.items = new ArrayDeque<>();
    }

    @Override
    public final T get(Set<Hint> hints) {
        while (items.isEmpty()) {
            Collection<T> loaded = nextItems(hints);
            if (loaded == null) {
                return null;
            }
            items.addAll(loaded);
        }
        return (items.isEmpty() ? null : items.removeFirst());
    }

    /**
     * @return The next items or <code>null</code> to indicate that no more items are available
     */
    protected abstract Collection<T> nextItems(Set<Hint> hints);
}
