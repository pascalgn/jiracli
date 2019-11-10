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
package com.github.pascalgn.jiracli.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Field;
import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.Text;
import com.github.pascalgn.jiracli.model.TextList;
import com.github.pascalgn.jiracli.util.Hint;
import com.github.pascalgn.jiracli.util.ReflectionUtils;
import com.github.pascalgn.jiracli.util.Supplier;

@CommandDescription(names = "properties", description = "Show the properties of the given objects")
class Properties implements Command {
    @Override
    public TextList execute(Context context, final Data input) {
        return new TextList(new Supplier<Text>() {
            private Iterator<Data> iterator;

            @Override
            public Text get(Set<Hint> hints) {
                if (iterator == null) {
                    iterator = input.toIterator(hints);
                }
                if (iterator.hasNext()) {
                    Data data = iterator.next();
                    if (data instanceof Issue) {
                        Issue issue = (Issue) data;
                        List<String> properties = new ArrayList<String>(ReflectionUtils.getProperties(issue));
                        for (Field field : issue.getFieldMap().getFields()) {
                            properties.add(field.getId());
                            addProperties(properties, field.getId(), field.getValue().get());
                        }
                        Collections.sort(properties);
                        return new Text(properties);
                    } else {
                        List<String> properties = new ArrayList<String>(ReflectionUtils.getProperties(data));
                        Collections.sort(properties);
                        return new Text(properties);
                    }
                } else {
                    return null;
                }
            }
        });
    }

    private static void addProperties(List<String> properties, String prefix, Object object) {
        if (object instanceof JSONObject) {
            JSONObject json = (JSONObject) object;
            for (String key : json.keySet()) {
                properties.add(prefix + "." + key);
                Object obj = json.get(key);
                addProperties(properties, prefix + "." + key, obj);
            }
        }
    }
}
