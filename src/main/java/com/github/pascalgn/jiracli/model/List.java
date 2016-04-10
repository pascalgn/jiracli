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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Supplier;

abstract class List<T extends Data> extends Data {
    private final Supplier<T> supplier;

    public List() {
        this(new Supplier<T>() {
            @Override
            public T get() {
                return null;
            }
        });
    }

    public List(final Iterator<T> iterator) {
        this(new Supplier<T>() {
            @Override
            public T get() {
                return iterator.hasNext() ? iterator.next() : null;
            }
        });
    }

    public List(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    protected Supplier<T> getSupplier() {
        return supplier;
    }

    public T next() {
        return supplier.get();
    }

    public java.util.List<T> remaining() {
        java.util.List<T> result = new ArrayList<T>();
        T item;
        while ((item = next()) != null) {
            result.add(item);
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Data> toList(Filter<Data> filter) {
        return (List<Data>) filteredList((Filter<T>) filter);
    }

    @Override
    public TextList toTextList() {
        return new TextList(convertingSupplier(new Function<T, Text>() {
            @Override
            public Text apply(T project) {
                return project.toText();
            }
        }));
    }

    public abstract List<T> filteredList(Filter<T> filter);

    public <R> Supplier<R> convertingSupplier(final Function<T, R> function) {
        return new Supplier<R>() {
            @Override
            public R get() {
                T next = next();
                return (next == null ? null : function.apply(next));
            }
        };
    }

    public <R> Supplier<R> loadingSupplier(final Function<T, Collection<R>> function) {
        return new Supplier<R>() {
            private Iterator<R> iterator;

            @Override
            public R get() {
                if (iterator == null || !iterator.hasNext()) {
                    T next = next();
                    if (next != null) {
                        Collection<R> collection = function.apply(next);
                        iterator = collection.iterator();
                    }
                }
                return (iterator.hasNext() ? iterator.next() : null);
            }
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
