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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class IOUtils {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static Reader createReader(File file) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file), CHARSET);
    }

    public static BufferedReader createBufferedReader(File file) throws FileNotFoundException {
        return new BufferedReader(createReader(file));
    }

    public static Writer createWriter(File file) throws FileNotFoundException {
        return new OutputStreamWriter(new FileOutputStream(file), CHARSET);
    }

    public static BufferedWriter createBufferedWriter(File file) throws FileNotFoundException {
        return new BufferedWriter(createWriter(file));
    }

    public static String toString(File file) {
        try {
            return toString0(createReader(file));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read from file: " + file, e);
        }
    }

    public static String toString(URL url) {
        try {
            return toString0(new InputStreamReader(url.openStream(), CHARSET));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read from URL: " + url, e);
        }
    }

    public static String toString(Reader reader) {
        try {
            return toString0(reader);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toString0(Reader reader) throws IOException {
        StringBuilder str = new StringBuilder();
        char[] buf = new char[2048];
        int len;
        while ((len = reader.read(buf)) != -1) {
            str.append(buf, 0, len);
        }
        return str.toString();
    }
}
