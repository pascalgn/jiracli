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
package com.github.pascalgn.jiracli.util;

import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtils {
    private static final Pattern JSON_OBJECT = Pattern.compile("^\\s*\\{");
    private static final Pattern JSON_ARRAY = Pattern.compile("^\\s*\\[");

    public static JSONObject toJsonObject(String str) {
        if (JSON_OBJECT.matcher(str).find()) {
            try {
                return new JSONObject(str);
            } catch (JSONException e) {
                // ignore!
            }
        }
        return null;
    }

    public static JSONArray toJsonArray(String str) {
        if (JSON_ARRAY.matcher(str).find()) {
            try {
                return new JSONArray(str);
            } catch (JSONException e) {
                // ignore!
            }
        }
        return null;
    }
}
