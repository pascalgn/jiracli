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
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Images {
    private static final Logger LOGGER = LoggerFactory.getLogger(Images.class);

    private static final String ROOT = "com/github/pascalgn/jiracli/";

    private static final List<Image> ICONS = readIcons(ROOT,
            Arrays.asList("icon-16x16.png", "icon-32x32.png", "icon-64x64.png", "icon-256x256.png"));
    private static final Image ICON_NO_BORDER = readIcon(ROOT + "icon-border-256x256.png");

    private static List<Image> readIcons(String root, List<String> names) {
        List<Image> icons = new ArrayList<Image>();
        for (String name : names) {
            Image image = readIcon(root + name);
            if (image != null) {
                icons.add(image);
            }
        }
        return icons;
    }

    private static Image readIcon(String path) {
        URL url = Images.class.getResource(path);
        if (url == null) {
            url = Images.class.getResource("/" + path);
        }
        if (url == null) {
            return null;
        }
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            LOGGER.warn("Error reading image: {}", path, e);
            return null;
        }
    }

    public static List<Image> getIcons() {
        return ICONS;
    }

    public static Image getIconNoBorder() {
        return ICON_NO_BORDER;
    }

    private Images() {
        // don't allow instances
    }
}
