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

public final class Step {
    private final String id;
    private final Status status;

    public Step(String id) {
        this.id = id;
        this.status = null;
    }

    public Step(String id, Status status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        final int prime = 59;
        return prime + id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Step other = (Step) obj;
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return "Step[id=" + id + ", status=" + status + "]";
    }
}
