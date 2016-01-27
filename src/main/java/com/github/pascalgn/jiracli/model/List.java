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
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Supplier;

abstract class List<T extends Type, E extends Data<?>> extends Data<T> {
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
}
