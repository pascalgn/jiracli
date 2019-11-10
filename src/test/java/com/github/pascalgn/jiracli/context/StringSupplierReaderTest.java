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
package com.github.pascalgn.jiracli.context;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.StringSupplierReader;
import com.github.pascalgn.jiracli.util.Supplier;

public class StringSupplierReaderTest {
    @Test
    public void testRead() throws Exception {
        final List<String> strs = new ArrayList<String>();
        strs.add("Hello ");
        strs.add("World");
        strs.add("!");

        Supplier<String> supplier = new Supplier<String>() {
            @Override
            public String get(Set<Hint> hints) {
                return (strs.isEmpty() ? null : strs.remove(0));
            }
        };

        StringBuilder result = new StringBuilder();
        try (Reader reader = new StringSupplierReader(supplier)) {
            char[] buffer = new char[4];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                result.append(buffer, 0, read);
            }
        }

        assertEquals("Hello World!", result.toString());
    }
}
