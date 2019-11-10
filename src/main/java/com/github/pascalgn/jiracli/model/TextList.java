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
import java.util.Set;

import com.github.pascalgn.jiracli.util.Function;
import com.github.pascalgn.jiracli.util.Functions;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.Supplier;

public class TextList extends List<Text> {
    private static final Function<String, Text> TO_TEXT = new Function<String, Text>() {
        @Override
        public Text apply(String str, Set<Hint> hints) {
            return new Text(str);
        }
    };

    public static Iterator<Text> toText(Iterator<String> iterator) {
        return Functions.convert(iterator, TO_TEXT);
    }

    public static Supplier<Text> toText(Supplier<String> supplier) {
        return Functions.convert(supplier, TO_TEXT);
    }

    private final String type;

    public TextList() {
        super();
        this.type = "";
    }

    public TextList(Text... texts) {
        this(Arrays.asList(texts).iterator());
    }

    public TextList(Iterator<Text> iterator) {
        this("", iterator);
    }

    public TextList(String type, Iterator<Text> iterator) {
        super(iterator);
        this.type = type;
    }

    public TextList(Supplier<Text> supplier) {
        this("", supplier);
    }

    public TextList(String type, Supplier<Text> supplier) {
        super(supplier);
        this.type = type;
    }

    public TextList(String type, TextList... lists) {
        super(combine(lists));
        this.type = type;
    }

    private static Supplier<Text> combine(final TextList[] lists) {
        final int[] index = { 0 };
        return new Supplier<Text>() {
            @Override
            public Text get(Set<Hint> hints) {
                while (index[0] < lists.length) {
                    TextList list = lists[index[0]];
                    Text next = list.next(hints);
                    if (next == null) {
                        index[0] += 1;
                    } else {
                        return next;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Returns the content type, for example <code>text/plain</code>. Never <code>null</code>, but can be empty.
     */
    public String getType() {
        return type;
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
        return new Text(type, str.toString());
    }

    @Override
    public TextList toTextList() {
        return this;
    }

    @Override
    public TextList filteredList(Filter<Text> filter) {
        return new TextList(type, new FilteredSupplier<>(getSupplier(), filter));
    }
}
