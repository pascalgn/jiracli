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
package com.github.pascalgn.jiracli.util;

import java.io.IOException;
import java.io.Reader;

public class StringSupplierReader extends Reader {
    private final Supplier<String> supplier;
    private final StringBuilder buffer;

    public StringSupplierReader(Supplier<String> supplier) {
        this.supplier = supplier;
        this.buffer = new StringBuilder();
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        while (buffer.length() < len) {
            String str = supplier.get(Hint.none());
            if (str == null) {
                if (buffer.length() == 0) {
                    return -1;
                }
                break;
            }
            buffer.append(str);
        }
        int avail = Math.min(len, buffer.length());
        buffer.getChars(0, avail, cbuf, off);
        buffer.delete(0, avail);
        return avail;
    }

    @Override
    public void close() throws IOException {
    }
}
