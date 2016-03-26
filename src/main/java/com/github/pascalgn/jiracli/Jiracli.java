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

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.DefaultConsole;
import com.github.pascalgn.jiracli.context.DefaultContext;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.DefaultWebService;
import com.github.pascalgn.jiracli.context.DelegateConsole;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.gui.ContextDialog;
import com.github.pascalgn.jiracli.gui.MainWindow;
import com.github.pascalgn.jiracli.util.Consumer;
import com.github.pascalgn.jiracli.util.Supplier;

/**
 * Main class
 */
public class Jiracli {
    public static String getTitle() {
        return "Jiracli 1.1.1-SNAPSHOT";
    }

    private static final String ROOT_URL = "rootURL";
    private static final String USERNAME = "username";

    private enum Option {
        HELP, VERSION, CONSOLE, GUI, ROOT_URL, USERNAME;
    }

    public static void main(String[] args) {
        Map<Option, Object> options = parse(args);
        if (options.get(Option.HELP) == Boolean.TRUE
                || (options.get(Option.CONSOLE) == Boolean.TRUE && options.get(Option.GUI) == Boolean.TRUE)) {
            System.out.println("usage: " + Jiracli.class.getName() + " [-h] [-g|-c] [<root-url>] [<username>]");
            System.out.println();
            System.out.println("JIRA Command Line Interface");
            System.out.println();
            System.out.println("options:");
            System.out.println("  -h, --help      show this help message");
            System.out.println("  -g, --gui       show a graphical console window");
            System.out.println("  -c, --console   run in console mode, using stdin and stdout");
            System.out.println("  --version       show the program version and exit");
            System.out.println("  <root-url>      the root URL of the JIRA service");
            System.out.println("  <username>      the username to use for authentication");
        } else if (options.get(Option.VERSION) == Boolean.TRUE) {
            System.out.println(getTitle().toLowerCase());
        } else {
            final boolean gui;
            if (options.get(Option.CONSOLE) == Boolean.TRUE) {
                gui = false;
            } else if (options.get(Option.GUI) == Boolean.TRUE) {
                gui = true;
            } else {
                gui = (System.console() == null && !GraphicsEnvironment.isHeadless());
            }

            if (gui) {
                startGUI((String) options.get(Option.USERNAME), (String) options.get(Option.USERNAME));
            } else {
                startConsole((String) options.get(Option.USERNAME), (String) options.get(Option.USERNAME));
            }
        }
    }

    private static Map<Option, Object> parse(String[] args) {
        List<String> list = new ArrayList<String>(Arrays.asList(args));
        Map<Option, Object> map = new HashMap<Option, Object>();
        map.put(Option.HELP, list.contains("-h") || list.contains("--help"));
        map.put(Option.CONSOLE, list.contains("-c") || list.contains("--console"));
        map.put(Option.GUI, list.contains("-g") || list.contains("--gui"));
        map.put(Option.VERSION, list.contains("--version"));
        list.removeAll(Arrays.asList("-h", "--help", "-c", "--console", "-g", "--gui", "--version"));
        if (!list.isEmpty()) {
            map.put(Option.ROOT_URL, list.remove(0));
        }
        if (!list.isEmpty()) {
            map.put(Option.USERNAME, list.remove(0));
        }
        if (!list.isEmpty()) {
            map.put(Option.HELP, true);
        }
        return map;
    }

    private static void startConsole(String givenRootURL, String givenUsername) {
        Console console = new DefaultConsole();

        final String rootURL;
        if (givenRootURL == null) {
            console.print("Root URL: ");
            rootURL = console.readLine();
        } else {
            rootURL = givenRootURL;
        }

        final String username;
        if (givenUsername == null) {
            console.print("Username: ");
            username = emptyToNull(console.readLine());
        } else {
            username = givenUsername;
        }

        final char[] password;
        if (username == null) {
            password = null;
        } else {
            console.print("Password: ");
            password = emptyToNull(console.readPassword());
        }

        WebService webService = new DefaultWebService(rootURL, username, password);
        JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console);
        Context context = new DefaultContext(console, webService, javaScriptEngine);

        context.onClose(new Runnable() {
            @Override
            public void run() {
                clearPassword(password);
            }
        });

        new Shell(context).start();
    }

    private static void startGUI(String givenRootURL, String givenUsername) {
        final Preferences preferences = Preferences.userNodeForPackage(Jiracli.class);
        String storedRootURL = preferences.get(ROOT_URL, givenRootURL);
        String storedUsername = preferences.get(USERNAME, givenUsername);

        final ContextDialog contextDialog = new ContextDialog(storedRootURL, storedUsername);
        contextDialog.setOkListener(new Runnable() {
            @Override
            public void run() {
                final String rootURL = contextDialog.getRootURL();
                final String username = emptyToNull(contextDialog.getUsername());
                final char[] password = emptyToNull(contextDialog.getPassword());

                preferences.put(ROOT_URL, rootURL);
                preferences.put(USERNAME, Objects.toString(username, ""));

                final MainWindow window = new MainWindow();

                Consumer<String> appendText = new Consumer<String>() {
                    @Override
                    public void accept(String str) {
                        window.appendText(str);
                    }
                };

                Supplier<String> readLine = new Supplier<String>() {
                    @Override
                    public String get() {
                        return window.readLine();
                    }
                };

                Console console = new DelegateConsole(appendText, readLine);

                WebService webService = new DefaultWebService(rootURL, username, password);
                JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console);
                final Context context = new DefaultContext(console, webService, javaScriptEngine);

                context.onClose(new Runnable() {
                    @Override
                    public void run() {
                        clearPassword(password);
                    }
                });

                Thread shellThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new Shell(context).start();

                        window.setVisible(false);
                        window.dispose();
                    }
                });
                shellThread.setDaemon(true);
                shellThread.start();

                contextDialog.setVisible(false);
                contextDialog.dispose();

                window.setVisible(true);
            }
        });
        contextDialog.setVisible(true);
    }

    private static String emptyToNull(String str) {
        return (str.isEmpty() ? null : str);
    }

    private static char[] emptyToNull(char[] str) {
        return (str.length == 0 ? null : str);
    }

    private static void clearPassword(char[] password) {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = '\0';
            }
        }
    }
}
