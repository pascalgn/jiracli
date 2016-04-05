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

import java.util.HashMap;
import java.util.Map;

public final class Cache<K, V> {
    private final Function<K, V> function;
    private final Map<K, V> map;

    public Cache(Function<K, V> function) {
        this.function = function;
        this.map = new HashMap<>();
    }

    public synchronized void putIfAbsent(K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, value);
        }
    }

    public synchronized V get(K key) {
        V value = map.get(key);
        if (value == null) {
            value = function.apply(key);
            if (value == null) {
                throw new IllegalStateException("Returned value must not be null!");
            }
            map.put(key, value);
        }
        return value;
    }

    public synchronized V getIfPresent(K key) {
        return map.get(key);
    }

    public synchronized void clear() {
        map.clear();
    }
}
