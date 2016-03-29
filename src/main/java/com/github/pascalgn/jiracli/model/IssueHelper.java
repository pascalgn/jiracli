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
package com.github.pascalgn.jiracli.model;

import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONWriter;

class IssueHelper {
    public static String toJson(Issue issue) {
        try (StringWriter stringWriter = new StringWriter()) {
            JSONWriter writer = new JSONWriter(stringWriter);
            writer.object();
            writer.key("key");
            writer.value(issue.getKey());
            writer.key("fields");
            writer.object();
            for (Field field : issue.getFieldMap().getFields()) {
                writer.key(field.getId());
                writer.value(field.getValue().getValue());
            }
            writer.endObject();
            writer.endObject();
            return stringWriter.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
