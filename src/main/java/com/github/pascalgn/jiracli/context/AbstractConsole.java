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
package com.github.pascalgn.jiracli.context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.github.pascalgn.jiracli.util.Credentials;

public abstract class AbstractConsole implements Console {
    private final Configuration configuration;
    private final Map<String, Credentials> credentials;

    public AbstractConsole(Configuration configuration) {
        this.configuration = configuration;
        this.credentials = new HashMap<>();
    }

    @Override
    public String getBaseUrl() {
        String baseUrl = configuration.getBaseUrl();
        if (baseUrl == null) {
            baseUrl = provideBaseUrl();
            if (baseUrl == null) {
                throw new IllegalStateException("No base URL provided!");
            }
            configuration.setBaseUrl(baseUrl);
        }
        return baseUrl;
    }

    protected abstract String provideBaseUrl();

    @Override
    public final Credentials getCredentials(String url) {
        Credentials c = credentials.get(url);
        if (c == null) {
            c = provideCredentials(configuration.getUsername(), url);
            if (c == null) {
                throw new IllegalStateException("No credentials provided!");
            }
        }
        return c;
    }

    protected abstract Credentials provideCredentials(String username, String url);

    protected boolean editFile(File file, boolean gui) {
        return EditorProvider.getEditor(gui).editFile(file);
    }
}
