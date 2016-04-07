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

import java.util.Objects;
import java.util.prefs.Preferences;

import com.github.pascalgn.jiracli.Constants;

public class DefaultConfiguration implements Configuration {
    private static final String BASE_URL = "baseUrl";
    private static final String USERNAME = "username";

    private final Preferences preferences;

    private String baseUrl;
    private String username;

    public DefaultConfiguration() {
        preferences = Constants.getPreferences();
        baseUrl = emptyToNull(preferences.get(BASE_URL, null));
        username = emptyToNull(preferences.get(USERNAME, null));
    }

    private static String emptyToNull(String str) {
        return (str == null || str.trim().isEmpty() ? null : str);
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = emptyToNull(baseUrl);
        preferences.put(BASE_URL, Objects.toString(baseUrl, ""));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = emptyToNull(username);
        preferences.put(USERNAME, Objects.toString(username, ""));
    }
}
