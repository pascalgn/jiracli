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

import java.util.Iterator;
import java.util.Set;

public class Functions {
    private static final Runnable EMPTY = new Runnable() {
        @Override
        public void run() {
        }
    };

    public static Runnable emptyRunnable() {
        return EMPTY;
    }

    public static <T, R> Iterator<R> convert(final Iterator<T> iterator, final Function<T, R> function) {
        return new Iterator<R>() {
            private R next;

            @Override
            public boolean hasNext() {
                if (next == null) {
                    readNext();
                }
                return next != null;
            }

            @Override
            public R next() {
                if (next == null) {
                    readNext();
                    if (next == null) {
                        throw new IllegalStateException("No more elements!");
                    }
                }
                R result = next;
                next = null;
                return result;
            }

            private void readNext() {
                if (iterator.hasNext()) {
                    next = function.apply(iterator.next(), Hint.none());
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static <T, R> Supplier<R> convert(final Supplier<T> supplier, final Function<T, R> function) {
        return new Supplier<R>() {
            @Override
            public R get(Set<Hint> hints) {
                T next = supplier.get(hints);
                return (next == null ? null : function.apply(next, hints));
            }
        };
    }
}
