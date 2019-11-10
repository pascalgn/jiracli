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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Hints to improve performance when requesting additional data
 */
public abstract class Hint {
    /**
     * Returns an empty set of hints
     */
    public static Set<Hint> none() {
        return Collections.emptySet();
    }

    /**
     * Returns a combination of the given hints
     */
    public static Set<Hint> combine(Set<Hint> hints1, Set<Hint> hints2) {
        if (hints1.isEmpty() && hints2.isEmpty()) {
            return hints1;
        } else if (!hints1.isEmpty() && hints2.isEmpty()) {
            return hints1;
        } else if (hints1.isEmpty() && !hints2.isEmpty()) {
            return hints2;
        } else {
            Set<Hint> hints = new HashSet<>(hints1);
            hints.addAll(hints2);
            return hints;
        }
    }

    /**
     * Returns a combination of the given hints
     */
    public static Set<Hint> combine(Set<Hint> hints, Hint hint) {
        if (hints.isEmpty()) {
            return Collections.singleton(hint);
        } else {
            Set<Hint> result = new HashSet<>(hints);
            result.add(hint);
            return result;
        }
    }
}
