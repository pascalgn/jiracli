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

import java.util.Collections;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Supplier;

public abstract class Data {
    private static final Filter<Data> ANY = new Filter<Data>() {
        @Override
        public Data get(Supplier<Data> supplier) {
            return supplier.get();
        }
    };

    public Iterator<Data> toIterator() {
        final List<Data> list = toList(ANY);
        if (list == null) {
            return Collections.singleton(this).iterator();
        } else {
            return new Iterator<Data>() {
                private Data next;

                @Override
                public boolean hasNext() {
                    if (next == null) {
                        next = list.next();
                    }
                    return (next != null);
                }

                @Override
                public Data next() {
                    Data result = next;
                    next = null;
                    return result;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    public List<Data> toList(Filter<Data> filter) {
        return null;
    }

    public final List<Data> toListOrFail(Filter<Data> filter) {
        return convertOrFail(toList(filter), List.class);
    }

    public Issue toIssue() {
        return null;
    }

    public final Issue toIssueOrFail() {
        return convertOrFail(toIssue(), Issue.class);
    }

    public IssueList toIssueList() {
        return null;
    }

    public final IssueList toIssueListOrFail() {
        return convertOrFail(toIssueList(), IssueList.class);
    }

    public Text toText() {
        return null;
    }

    public final Text toTextOrFail() {
        return convertOrFail(toText(), Text.class);
    }

    public TextList toTextList() {
        return null;
    }

    public final TextList toTextListOrFail() {
        return convertOrFail(toTextList(), TextList.class);
    }

    public Field toField() {
        return null;
    }

    public final Field toFieldOrFail() {
        return convertOrFail(toField(), Field.class);
    }

    public FieldList toFieldList() {
        return null;
    }

    public final FieldList toFieldListOrFail() {
        return convertOrFail(toFieldList(), FieldList.class);
    }

    public AttachmentList toAttachmentList() {
        return null;
    }

    public final AttachmentList toAttachmentListOrFail() {
        return convertOrFail(toAttachmentList(), AttachmentList.class);
    }

    public BoardList toBoardList() {
        return null;
    }

    public final BoardList toBoardListOrFail() {
        return convertOrFail(toBoardList(), BoardList.class);
    }

    public SprintList toSprintList() {
        return null;
    }

    public final SprintList toSprintListOrFail() {
        return convertOrFail(toSprintList(), SprintList.class);
    }

    public ProjectList toProjectList() {
        return null;
    }

    public final ProjectList toProjectListOrFail() {
        return convertOrFail(toProjectList(), ProjectList.class);
    }

    private <T> T convertOrFail(T instance, Class<? super T> type) {
        if (instance == null) {
            throw new IllegalStateException("Could not convert to " + type.getSimpleName() + ": " + this);
        }
        return instance;
    }
}
