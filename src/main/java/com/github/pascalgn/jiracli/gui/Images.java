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
package com.github.pascalgn.jiracli.gui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Images {
    private static final Logger LOGGER = LoggerFactory.getLogger(Images.class);

    private static final List<Image> ICONS = readIcons();

    private static List<Image> readIcons() {
        String root = "com/github/pascalgn/jiracli/";
        String[] names = { "icon-16x16.png", "icon-32x32.png", "icon-64x64.png", "icon-256x256.png" };
        List<Image> icons = new ArrayList<Image>();
        for (String name : names) {
            URL url = Images.class.getResource(root + name);
            if (url == null) {
                url = Images.class.getResource("/" + root + name);
            }
            if (url == null) {
                continue;
            }
            Image image;
            try {
                image = ImageIO.read(url);
            } catch (IOException e) {
                LOGGER.warn("Error reading image: {}", name, e);
                continue;
            }
            if (image != null) {
                icons.add(image);
            }
        }
        return icons;
    }

    public static List<Image> getIcons() {
        return ICONS;
    }

    private Images() {
        // don't allow instances
    }
}
