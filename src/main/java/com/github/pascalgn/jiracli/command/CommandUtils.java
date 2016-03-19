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
package com.github.pascalgn.jiracli.command;

import java.io.Closeable;
import java.io.IOException;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.WebService;

class CommandUtils {
    public static void closeUnchecked(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            throw new IllegalStateException("Exception while trying to close: " + closeable, e);
        }
    }

    public static Object getFieldValue(WebService webService, JSONObject json, String name) {
        if (name.contains(".")) {
            String[] names = name.split("\\.");
            Object obj = getFieldValue(webService, json, names[0]);
            for (int i = 1; i < names.length; i++) {
                obj = ((JSONObject) obj).get(names[i]);
            }
            return obj;
        } else {
            if (json.has(name)) {
                return json.get(name);
            } else {
                JSONObject fields = json.getJSONObject("fields");
                if (fields.has(name)) {
                    return fields.get(name);
                }

                // Try custom field names:
                String fieldId = webService.getFieldMapping().get(name);
                if (fieldId != null) {
                    if (json.has(fieldId)) {
                        return json.get(fieldId);
                    } else if (fields.has(fieldId)) {
                        return fields.get(fieldId);
                    }
                }

                throw new IllegalStateException("Name '" + name + "' not found: " + json.toString(2));
            }
        }
    }
}
