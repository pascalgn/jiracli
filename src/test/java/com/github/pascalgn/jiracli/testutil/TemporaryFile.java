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
package com.github.pascalgn.jiracli.testutil;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TemporaryFile implements Closeable {
    private final File file;

    public TemporaryFile() throws IOException {
        this("temp", ".txt");
    }

    public TemporaryFile(String prefix, String suffix) throws IOException {
        file = File.createTempFile(prefix, suffix);
    }

    public File getFile() {
        return file;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }
    
    public OutputStream createOutputStream() throws FileNotFoundException {
        return new FileOutputStream(file);
    }

    @Override
    public void close() throws IOException {
        if (!file.delete() && file.exists()) {
            throw new IOException("Cannot delete file: " + file);
        }
    }
}
