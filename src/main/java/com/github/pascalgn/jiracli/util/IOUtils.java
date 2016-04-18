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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IOUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtils.class);

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    static final int BUFFER_SIZE = 4096;

    public static Reader createReader(File file) throws FileNotFoundException {
        return new InputStreamReader(new FileInputStream(file), CHARSET);
    }

    public static BufferedReader createBufferedReader(File file) throws FileNotFoundException {
        return new BufferedReader(createReader(file));
    }

    public static Writer createWriter(File file) throws FileNotFoundException {
        return createWriter(file, CHARSET);
    }

    public static Writer createWriter(File file, Charset charset) throws FileNotFoundException {
        return new OutputStreamWriter(new FileOutputStream(file), charset);
    }

    public static BufferedWriter createBufferedWriter(File file) throws FileNotFoundException {
        return new BufferedWriter(createWriter(file));
    }

    public static String toString(File file) {
        try (Reader reader = createReader(file)) {
            return toString0(reader);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read from file: " + file, e);
        }
    }

    public static String toString(URL url) {
        try (Reader reader = new InputStreamReader(url.openStream(), CHARSET)) {
            return toString0(reader);
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

    public static String toString(Reader reader, int maxLength) {
        try {
            return toString0(reader, maxLength);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toString0(Reader reader) throws IOException {
        StringBuilder str = new StringBuilder();
        char[] buf = new char[BUFFER_SIZE];
        int len;
        while ((len = reader.read(buf)) != -1) {
            str.append(buf, 0, len);
        }
        return str.toString();
    }

    private static String toString0(Reader reader, int maxLength) throws IOException {
        if (maxLength < 0) {
            throw new IllegalArgumentException("Invalid length: " + maxLength);
        } else if (maxLength == 0) {
            return "";
        } else {
            int size = Math.min(maxLength, BUFFER_SIZE);
            StringBuilder str = new StringBuilder();
            char[] buf = new char[size];
            int len;
            while ((len = reader.read(buf)) != -1) {
                if (str.length() + len < maxLength) {
                    str.append(buf, 0, len);
                } else {
                    str.append(buf, 0, maxLength - str.length());
                    break;
                }
            }
            return str.toString();
        }
    }

    public static void copy(InputStream input, OutputStream output) {
        byte[] buf = new byte[BUFFER_SIZE];
        try {
            int len;
            while ((len = input.read(buf)) != -1) {
                output.write(buf, 0, len);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void write(File file, String content) {
        write(file, CHARSET, content);
    }

    public static void write(File file, Charset charset, String content) {
        try (Writer writer = createWriter(file, charset)) {
            writer.write(content);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a temporary file and deletes it after the function has completed
     */
    public static <T> T withTemporaryFile(String prefix, String suffix, Function<File, T> function) {
        try {
            File tempFile = File.createTempFile(prefix, suffix);
            try {
                return function.apply(tempFile, Hint.none());
            } finally {
                if (!tempFile.delete() && tempFile.exists()) {
                    LOGGER.warn("Could not delete temporary file: {}", tempFile);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a file object for the given path, expanding "~" to the home directory
     */
    public static File getFile(String path) {
        File file;
        if (path.startsWith("~/") || path.startsWith("~\\")) {
            file = new File(getHome(), path.substring(1));
        } else {
            file = new File(path);
        }
        return file.getAbsoluteFile();
    }

    private static File getHome() {
        String property = System.getProperty("user.home");
        File home = new File(property);
        if (!home.isDirectory()) {
            throw new IllegalStateException("Invalid home directory: " + property);
        }
        return home;
    }
}
