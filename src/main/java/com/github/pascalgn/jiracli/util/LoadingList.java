/*
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

import java.util.AbstractList;
import java.util.List;

public abstract class LoadingList<T> extends AbstractList<T> {
    private List<T> delegate;

    private synchronized List<T> getDelegate() {
        if (delegate == null) {
            delegate = loadList();
            if (delegate == null) {
                throw new NullPointerException("Implementation: " + getClass());
            }
        }
        return delegate;
    }

    protected abstract List<T> loadList();

    @Override
    public T get(int index) {
        return getDelegate().get(index);
    }

    @Override
    public int size() {
        return getDelegate().size();
    }
}
