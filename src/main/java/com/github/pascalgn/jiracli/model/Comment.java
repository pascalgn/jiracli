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
package com.github.pascalgn.jiracli.model;

import java.util.Arrays;
import java.util.Date;

import com.github.pascalgn.jiracli.util.ConversionUtils;

public final class Comment extends Data {
    private final int id;

    private final User author;
    private final String body;
    private final Date created;

    public Comment(int id, User author, String body, Date created) {
        this.id = id;
        this.author = author;
        this.body = body;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getBody() {
        return body;
    }

    public Date getCreated() {
        return created;
    }

    @Override
    public Text toText() {
        return new Text(Arrays.asList(author.getName(), ConversionUtils.formatDate(created), body));
    }

    @Override
    public String toString() {
        return "Comment[id=" + id + ", author=" + author + ", body=" + body + ", created=" + created + "]";
    }
}
