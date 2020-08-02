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

public class IssueList extends List<Issue> {
    public IssueList() {
        super();
    }

    public IssueList(Issue... issues) {
        super(Arrays.asList(issues).iterator());
    }

    public IssueList(Iterator<Issue> iterator) {
        super(iterator);
    }

    public IssueList(Supplier<Issue> supplier) {
        super(supplier);
    }

    @Override
    public IssueList toIssueList() {
        return this;
    }

    @Override
    public <E> E accept(ListVisitor<E> visitor) {
        return visitor.visit(this);
    }

    @Override
    public IssueList filteredList(Filter<Issue> filter) {
        return new IssueList(new FilteredSupplier<>(getSupplier(), filter));
    }
}
