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

import java.util.Objects;

import com.github.pascalgn.jiracli.util.StringUtils;

public class Text extends Data {
    private final String type;
    private final String text;

    public Text(String text) {
        this.type = "";
        this.text = Objects.requireNonNull(text);
    }

    public Text(String type, String text) {
        this.type = Objects.requireNonNull(type);
        this.text = Objects.requireNonNull(text);
    }

    public Text(java.util.List<String> texts) {
        this.type = "";
        if (texts.isEmpty()) {
            this.text = "";
        } else {
            this.text = StringUtils.join(texts, System.lineSeparator());
        }
    }

    /**
     * Returns the content type, for example <code>text/plain</code>. Never <code>null</code>, but can be empty.
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the stored text content
     */
    public String getText() {
        return text;
    }

    @Override
    public Text toText() {
        return this;
    }

    @Override
    public TextList toTextList() {
        return (text.isEmpty() ? new TextList() : new TextList(this));
    }

    @Override
    public int hashCode() {
        return 31 + text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Text other = (Text) obj;
        return text.equals(other.text);
    }

    @Override
    public String toString() {
        return text;
    }
}
