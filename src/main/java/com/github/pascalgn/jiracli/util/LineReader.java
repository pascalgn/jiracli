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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Like {@link BufferedReader}, but returns the end-of-line characters when reading lines
 */
public class LineReader implements Closeable {
    public static List<String> readLines(String str) {
        try (StringReader reader = new StringReader(str)) {
            try {
                return readLines(reader);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static List<String> readLines(Reader reader) throws IOException {
        List<String> lines = new ArrayList<String>();
        @SuppressWarnings("resource")
        LineReader lineReader = new LineReader(reader);
        String line;
        while ((line = lineReader.readLine()) != null) {
            lines.add(line);
        }
        return lines;
    }

    private final BufferedReader reader;

    public LineReader(Reader reader) {
        this.reader = toBufferedReader(reader);
    }

    private static BufferedReader toBufferedReader(Reader reader) {
        return (reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader));
    }

    public String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        char[] buf = new char[1];
        int len;
        while ((len = reader.read(buf)) != -1) {
            if (len > 0) {
                line.append(buf, 0, len);
                // possible EOLs: \n, \r, \r\n
                if (buf[0] == '\n') {
                    break;
                } else if (buf[0] == '\r') {
                    reader.mark(1);
                    int c = reader.read();
                    if (c == -1) {
                        break;
                    } else if (c == '\n') {
                        line.append("\n");
                        break;
                    } else {
                        reader.reset();
                    }
                }
            }
        }
        String str = line.toString();
        return (len == -1 && str.isEmpty() ? null : str);
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
