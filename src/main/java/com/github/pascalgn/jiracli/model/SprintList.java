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
package com.github.pascalgn.jiracli.model;

import java.util.Arrays;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Supplier;

public class SprintList extends List<Sprint> {
    public SprintList() {
        super();
    }

    public SprintList(Sprint... sprints) {
        super(Arrays.asList(sprints).iterator());
    }

    public SprintList(Iterator<Sprint> iterator) {
        super(iterator);
    }

    public SprintList(Supplier<Sprint> supplier) {
        super(supplier);
    }

    @Override
    public SprintList toSprintList() {
        return this;
    }

    @Override
    public <E> E accept(ListVisitor<E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public SprintList filteredList(Filter<Sprint> filter) {
        return new SprintList(new FilteredSupplier<>(getSupplier(), filter));
    }
}
