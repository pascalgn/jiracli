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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Supplier;

abstract class List<E extends Data> extends Data {
    private final Supplier<E> supplier;

    public List() {
        this(new Supplier<E>() {
            @Override
            public E get() {
                return null;
            }
        });
    }

    public List(final Iterator<E> iterator) {
        this(new Supplier<E>() {
            @Override
            public E get() {
                return iterator.hasNext() ? iterator.next() : null;
            }
        });
    }

    public List(Supplier<E> supplier) {
        this.supplier = supplier;
    }

    public E next() {
        return supplier.get();
    }

    public java.util.List<E> remaining() {
        java.util.List<E> result = new ArrayList<E>();
        E item;
        while ((item = next()) != null) {
            result.add(item);
        }
        return result;
    }

    public IssueList toIssueList(final Function<E, Issue> function) {
        return new IssueList(new Supplier<Issue>() {
            @Override
            public Issue get() {
                E next = next();
                return (next == null ? null : function.apply(next));
            }
        });
    }

    public TextList toTextList(final Function<E, Text> function) {
        return new TextList(new Supplier<Text>() {
            @Override
            public Text get() {
                E next = next();
                return (next == null ? null : function.apply(next));
            }
        });
    }

    public FieldList toFieldList(final Function<E, Field> function) {
        return new FieldList(new Supplier<Field>() {
            @Override
            public Field get() {
                E next = next();
                return (next == null ? null : function.apply(next));
            }
        });
    }

    public <R> Supplier<R> loadingSupplier(final Function<E, Collection<R>> function) {
        final Deque<R> deque = new ArrayDeque<>();
        return new Supplier<R>() {
            @Override
            public R get() {
                if (deque.isEmpty()) {
                    E next = next();
                    if (next != null) {
                        Collection<R> collection = function.apply(next);
                        deque.addAll(collection);
                    }
                }
                return (deque.isEmpty() ? null : deque.removeFirst());
            }
        };
    }
}
