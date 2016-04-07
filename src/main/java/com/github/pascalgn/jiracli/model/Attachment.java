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

import java.net.URI;

public class Attachment extends Data {
    private final Issue issue;

    private final int id;
    private final String filename;

    private final String mimeType;
    private final long size;
    private final URI content;

    public Attachment(Issue issue, int id, String filename, String mimeType, long size, URI content) {
        this.issue = issue;
        this.id = id;
        this.filename = filename;
        this.mimeType = mimeType;
        this.size = size;
        this.content = content;
    }

    public Issue getIssue() {
        return issue;
    }

    public int getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSize() {
        return size;
    }

    public URI getContent() {
        return content;
    }

    @Override
    public Text toText() {
        return new Text(getFilename());
    }

    @Override
    public TextList toTextList() {
        return new TextList(toText());
    }

    @Override
    public String toString() {
        return "Attachment [id=" + id + ", filename=" + filename + ", mimeType=" + mimeType + ", size=" + size
                + ", content=" + content + "]";
    }
}
