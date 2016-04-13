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

import java.util.Arrays;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

public class TextList extends List<Text> {
    public TextList() {
        super();
    }

    public TextList(Text... texts) {
        super(Arrays.asList(texts).iterator());
    }

    public TextList(Iterator<Text> iterator) {
        super(iterator);
    }

    public TextList(Supplier<Text> supplier) {
        super(supplier);
    }

    @Override
    public Text toText() {
        return toText(System.lineSeparator());
    }

    public Text toText(String separator) {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        Text text;
        while ((text = next(Hint.none())) != null) {
            if (first) {
                first = false;
            } else {
                str.append(separator);
            }
            str.append(text.getText());
        }
        return new Text(str.toString());
    }

    @Override
    public TextList toTextList() {
        return this;
    }

    @Override
    public TextList filteredList(Filter<Text> filter) {
        return new TextList(new FilteredSupplier<>(getSupplier(), filter));
    }
}
