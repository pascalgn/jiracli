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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.model.Attachment;
import com.github.pascalgn.jiracli.model.AttachmentList;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.IOUtils;

@CommandDescription(names = { "download", "dl" }, description = "Download the given attachments")
class Download implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(Download.class);

    @Argument(parameters = Parameters.ONE, variable = "<dir>",
            description = "the local directory where the attachments will be stored")
    private String path;

    @Override
    public Data execute(final Context context, Data input) {
        File directory = new File(path);
        if (!directory.isAbsolute() || !directory.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + path);
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + path);
        }

        AttachmentList attachmentList = input.toAttachmentListOrFail();
        List<Attachment> attachments = attachmentList.remaining();

        final Console console = context.getConsole();

        for (Attachment attachment : attachments) {
            final File file = new File(directory, attachment.getFilename());
            boolean createNewFile;
            try {
                createNewFile = file.createNewFile();
            } catch (IOException e) {
                LOGGER.info("Error creating file: {}", file, e);
                console.println("Could not create file: " + file);
                continue;
            }
            if (createNewFile) {
                final URI uri = attachment.getContent();
                console.println("Downloading " + file.getName());
                context.getWebService().download(uri, new Consumer<InputStream>() {
                    @Override
                    public void accept(InputStream input) {
                        try (OutputStream output = new FileOutputStream(file)) {
                            IOUtils.copy(input, output);
                        } catch (IOException e) {
                            LOGGER.info("Error downloading URL: {}", uri, e);
                            console.println("Could not download file!");
                        }
                    }
                });
            } else {
                console.println("File exists: " + file.getName());
            }
        }

        return None.getInstance();
    }
}
