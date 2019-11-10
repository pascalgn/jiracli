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
package com.github.pascalgn.jiracli.gui;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class Images {
    private static final Logger LOGGER = LoggerFactory.getLogger(Images.class);

    private static final String ROOT = "com/github/pascalgn/jiracli/";

    private static final Image ICON_A = readIcon(ROOT + "icon-a-256x256.png");
    private static final Image ICON_B = readIcon(ROOT + "icon-b-256x256.png");

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

    public static Image getIconsA() {
        return ICON_A;
    }

    public static Image getIconB() {
        return ICON_B;
    }

    private Images() {
        // don't allow instances
    }
}
