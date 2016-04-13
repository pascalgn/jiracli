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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StringUtils {
    public static List<String> splitNewline(String str) {
        List<String> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(str))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static List<String> split(String str, String delimiter) {
        List<String> result = new ArrayList<>();
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("Empty delimiter!");
        }
        int position = 0;
        while (position < str.length()) {
            int old = position;
            int pos = str.indexOf(delimiter, position);
            if (pos == -1) {
                break;
            } else {
                result.add(str.substring(old, pos));
                position = pos + delimiter.length();
            }
        }
        if (position <= str.length()) {
            result.add(str.substring(position, str.length()));
        }
        return result;
    }

    public static String join(Iterable<?> items, String sep) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Object obj : items) {
            if (first) {
                first = false;
            } else {
                result.append(sep);
            }
            result.append(obj);
        }
        return result.toString();
    }

    public static String repeat(String str, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }

    /**
     * Strips <b>all</b> occurrences of <code>end</code> from the end of <code>str</code>
     */
    public static String stripEnd(String str, String end) {
        if (end.isEmpty()) {
            return str;
        } else {
            String s = str;
            while (s.endsWith(end)) {
                s = s.substring(0, s.length() - end.length());
            }
            return s;
        }
    }

    public static String shorten(String str, int length) {
        if (length < 3) {
            throw new IllegalArgumentException("Invalid length");
        }
        String s;
        if (str.length() <= length) {
            s = str;
        } else {
            s = str.substring(0, length - 3) + "...";
        }
        s = s.replaceAll("[\\r\\n]+", " ");
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    public static String capitalize(String str) {
        if (str.isEmpty()) {
            return str;
        } else {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }
}
