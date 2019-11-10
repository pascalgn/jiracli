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
package com.github.pascalgn.jiracli.testserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class Resources {
    private static final String PATH_PREFIX = "/rest/api/latest";
    private static final String RESOURCE_PREFIX = "/com/github/pascalgn/jiracli/testserver";

    private final Map<String, String> resources;

    public Resources() {
        resources = new HashMap<String, String>();
        putResource("/issue/JRA-123", "JRA-123.issue.json");
        putResource("/issue/JRA-123/editmeta", "JRA-123.editmeta.json");
        putResource("/search", "search.key_JRA-123.json");
        putResource("/field", "field.json");
    }

    private void putResource(String path, String resource) {
        InputStream input = getClass().getResourceAsStream(RESOURCE_PREFIX + "/" + resource);
        if (input == null) {
            throw new IllegalStateException("Could not read resource: " + resource);
        }
        resources.put(PATH_PREFIX + path, toString(input));
    }

    private static String toString(InputStream input) {
        StringBuilder str = new StringBuilder();
        byte[] buf = new byte[2048];
        int len;
        try {
            while ((len = input.read(buf)) != -1) {
                str.append(new String(buf, 0, len));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error reading input stream", e);
        }
        return str.toString();
    }

    public String getResource(String path) {
        return getResource(path, Collections.<String, String> emptyMap());
    }

    public String getResource(String path, Map<String, String> parameters) {
        return resources.get(path);
    }
}
