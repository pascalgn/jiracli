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
package com.github.pascalgn.jiracli;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

import com.github.pascalgn.jiracli.command.Context;
import com.github.pascalgn.jiracli.command.DefaultContext;
import com.github.pascalgn.jiracli.command.DefaultWebService;
import com.github.pascalgn.jiracli.command.WebService;
import com.github.pascalgn.jiracli.console.Console;
import com.github.pascalgn.jiracli.console.DefaultConsole;

/**
 * Main class
 */
public class App {
    private static final String BASE64_PREFIX = "base64:";

    public static void main(String[] args) throws IOException {
        start(args, new DefaultConsole());
    }

    private static void start(String[] args, Console console) throws IOException {
        String rootURL;
        String username;
        String password;
        if (args.length == 0) {
            console.print("Root URL: ");
            rootURL = console.readLine();
            console.print("Username: ");
            username = emptyToNull(console.readLine());
            console.print("Password: ");
            password = emptyToNull(console.readLine());
        } else if (args.length == 1) {
            rootURL = args[0].trim();
            username = null;
            password = null;
        } else if (args.length == 3) {
            rootURL = args[0].trim();
            username = args[1].trim();
            password = getPassword(args[2]);
        } else {
            throw new IllegalArgumentException("usage: " + App.class.getName() + " ROOT_URL [USERNAME] [PASSWORD]");
        }

        WebService webService = new DefaultWebService(rootURL, username, password);
        Context context = new DefaultContext(console, webService);

        new Shell(context).start();
    }

    private static String emptyToNull(String str) {
        return (str.isEmpty() ? null : str);
    }

    private static String getPassword(String str) {
        if (str.startsWith(BASE64_PREFIX)) {
            return new String(Base64.decodeBase64(str.substring(BASE64_PREFIX.length())), StandardCharsets.UTF_8);
        } else {
            return str;
        }
    }
}
