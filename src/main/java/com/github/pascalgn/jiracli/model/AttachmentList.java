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

public class AttachmentList extends List<Attachment> {
    public AttachmentList() {
        super();
    }

    public AttachmentList(Attachment... attachments) {
        super(Arrays.asList(attachments).iterator());
    }

    public AttachmentList(Iterator<Attachment> iterator) {
        super(iterator);
    }

    public AttachmentList(Supplier<Attachment> supplier) {
        super(supplier);
    }

    @Override
    public AttachmentList toAttachmentList() {
        return this;
    }

    @Override
    public AttachmentList filteredList(Filter<Attachment> filter) {
        return new AttachmentList(new FilteredSupplier<>(getSupplier(), filter));
    }
}
