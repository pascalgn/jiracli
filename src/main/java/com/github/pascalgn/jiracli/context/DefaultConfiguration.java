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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.Constants;
import com.github.pascalgn.jiracli.util.IOUtils;
import com.github.pascalgn.jiracli.util.StringUtils;
import com.github.pascalgn.jiracli.util.SystemUtils;

public class DefaultConfiguration implements Configuration {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfiguration.class);

    private static final String BASE_URL = "baseUrl";
    private static final String USERNAME = "username";

    private final Preferences preferences;
    private final File home;

    private final File historyFile;

    private String baseUrl;
    private String username;

    private List<String> history;

    public DefaultConfiguration() {
        preferences = Constants.getPreferences();
        home = SystemUtils.getHome(Constants.getName());

        historyFile = new File(home, "history");

        baseUrl = emptyToNull(preferences.get(BASE_URL, null));
        if (baseUrl != null) {
            baseUrl = StringUtils.stripEnds(baseUrl.trim(), "/");
        }

        username = emptyToNull(preferences.get(USERNAME, null));
        if (username != null) {
            username = username.trim();
        }

        history = readHistory(historyFile);
    }

    private static List<String> readHistory(File file) {
        if (file.exists()) {
            List<String> result;
            try (BufferedReader reader = IOUtils.createBufferedReader(file)) {
                result = new ArrayList<String>();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.add(line);
                }
            } catch (FileNotFoundException e) {
                result = Collections.emptyList();
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read file: " + file, e);
            }
            return result;
        } else {
            return Collections.emptyList();
        }
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
        String url = emptyToNull(baseUrl);
        if (url != null) {
            url = StringUtils.stripEnds(url.trim(), "/");
            url = emptyToNull(url);
        }
        this.baseUrl = url;
        preferences.put(BASE_URL, Objects.toString(url, ""));
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

    @Override
    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    @Override
    public void setHistory(List<String> history) {
        Objects.requireNonNull(history);
        this.history = new ArrayList<String>(history);
    }

    @Override
    public void close() {
        writeHistory(history, historyFile);
    }

    private static void writeHistory(List<String> history, File file) {
        try (BufferedWriter writer = IOUtils.createBufferedWriter(file)) {
            for (String line : history) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to write file: {}", file, e);
        }
    }
}
