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
package com.github.pascalgn.jiracli.util;

import java.io.File;
import java.util.Locale;

/**
 * OS specific utility methods
 */
public class SystemUtils {
    public enum OS {
        MAC, WINDOWS, UNIX;
    }

    public static OS getOS() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin")) {
            return OS.MAC;
        } else if (os.contains("win")) {
            return OS.WINDOWS;
        } else {
            return OS.UNIX;
        }
    }

    /**
     * Gets the home directory for the specified application name and creates it, if necessary
     */
    public static File getHome(String applicationName) {
        String homeProperty = System.getProperty("user.home");
        if (homeProperty == null) {
            throw new IllegalStateException("Property user.home not set!");
        }

        File home = new File(homeProperty);
        if (!home.exists()) {
            throw new IllegalStateException("Home directory does not exist: " + home);
        }

        File result;
        switch (getOS()) {
        case MAC:
            result = new File(home, "Library" + File.separator + applicationName).getAbsoluteFile();
            break;

        case WINDOWS:
            String path = "AppData" + File.separator + "Local" + File.separator + applicationName;
            result = new File(home, path).getAbsoluteFile();
            break;

        case UNIX:
            result = new File(home, "." + applicationName.toLowerCase()).getAbsoluteFile();
            break;

        default:
            throw new IllegalStateException();
        }

        if (!result.isDirectory()) {
            if (!result.mkdir() && !result.isDirectory()) {
                throw new IllegalStateException("Directory does not exist and cannot be created: " + result);
            }
        }

        return result;
    }
}
