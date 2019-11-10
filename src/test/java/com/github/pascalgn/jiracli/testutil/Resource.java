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
package com.github.pascalgn.jiracli.testutil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import com.github.pascalgn.jiracli.util.IOUtils;

public final class Resource {
    public static Resource get(Class<?> type, String name) {
        String folder = getPackage(type.getName(), type.getSimpleName()).replace(".", "/");
        String fullName = (folder.isEmpty() ? "" : folder + "/") + name;
        URL url = type.getResource(fullName);
        if (url == null) {
            url = type.getResource("/" + fullName);
        }
        return new Resource(url);
    }

    private static String getPackage(String name, String simpleName) {
        if (name.equals(simpleName)) {
            return "";
        } else {
            return name.substring(0, name.length() - (1 + simpleName.length()));
        }
    }

    private final URL url;

    private Resource(URL url) {
        this.url = url;
    }

    public Reader openReader() throws IOException {
        return new InputStreamReader(url.openStream());
    }

    public String getContents() {
        return IOUtils.toString(url);
    }

    @Override
    public String toString() {
        return "Resource[" + url + "]";
    }
}
