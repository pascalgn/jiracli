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

import java.util.Date;
import java.util.List;

public final class Change {
    private final int id;
    private final User user;
    private final Date date;
    private final List<Item> items;

    public Change(int id, User user, Date date, List<Item> items) {
        this.id = id;
        this.user = user;
        this.date = date;
        this.items = items;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }

    public List<Item> getItems() {
        return items;
    }

    public static final class Item {
        private final String field;
        private final String from;
        private final String to;

        public Item(String field, String from, String to) {
            this.field = field;
            this.from = from;
            this.to = to;
        }

        public String getField() {
            return field;
        }

        public String getFrom() {
            return from;
        }

        public String getTo() {
            return to;
        }
    }
}
