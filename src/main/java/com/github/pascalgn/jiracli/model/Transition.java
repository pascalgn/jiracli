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

public final class Transition {
    private final int id;
    private final String name;
    private final Step source;
    private final Step target;

    public Transition(int id, String name, Step source, Step target) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.target = target;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Step getSource() {
        return source;
    }

    public Step getTarget() {
        return target;
    }

    public boolean isGlobal() {
        return source.equals(target);
    }

    @Override
    public String toString() {
        return "Transition[id=" + id + ", name=" + name + "]";
    }
}
