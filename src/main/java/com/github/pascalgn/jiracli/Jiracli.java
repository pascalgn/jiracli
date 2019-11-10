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
package com.github.pascalgn.jiracli;

import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.context.Configuration;
import com.github.pascalgn.jiracli.context.Console;
import com.github.pascalgn.jiracli.context.Context;
import com.github.pascalgn.jiracli.context.DefaultConfiguration;
import com.github.pascalgn.jiracli.context.DefaultConsole;
import com.github.pascalgn.jiracli.context.DefaultContext;
import com.github.pascalgn.jiracli.context.DefaultJavaScriptEngine;
import com.github.pascalgn.jiracli.context.JavaScriptEngine;
import com.github.pascalgn.jiracli.context.WebService;
import com.github.pascalgn.jiracli.gui.ConsoleWindow;
import com.github.pascalgn.jiracli.web.DefaultWebService;

/**
 * Main class
 */
public class Jiracli {
    private static final Logger LOGGER = LoggerFactory.getLogger(Jiracli.class);

    private static final AtomicInteger SHELL_THREAD_INDEX = new AtomicInteger(0);

    private enum Option {
        HELP, VERSION, CONSOLE, GUI, CMD_LINE;
    }

    public static void main(String[] args) {
        Map<Option, Object> options = parse(args);
        if (options.get(Option.HELP) == Boolean.TRUE
                || (options.get(Option.CONSOLE) == Boolean.TRUE && options.get(Option.GUI) == Boolean.TRUE)) {
            System.out.println("usage: " + Jiracli.class.getSimpleName() + " [-h] [-V] [-g|-c] [--command <line>]");
            System.out.println();
            System.out.println("Jira Command Line Interface");
            System.out.println();
            System.out.println("options:");
            System.out.println("  -h, --help      show this help message");
            System.out.println("  -g, --gui       show a graphical console window");
            System.out.println("  -c, --console   run in console mode, using stdin and stdout");
            System.out.println("  -V, --version       show the program version and exit");
            System.out.println("  --command <line>    execute the line as a command");
        } else if (options.get(Option.VERSION) == Boolean.TRUE) {
            System.out.println(Constants.getTitle());
        } else if (options.get(Option.CMD_LINE) != null) {

            LOGGER.trace("Executing command {}...", options.get(Option.CMD_LINE));
            executeCommand((String)options.get(Option.CMD_LINE));

        } else {
            LOGGER.debug("Starting {}...", Constants.getTitle());

            final boolean gui;
            if (options.get(Option.CONSOLE) == Boolean.TRUE) {
                gui = false;
            } else if (options.get(Option.GUI) == Boolean.TRUE) {
                gui = true;
            } else {
                gui = (System.console() == null && !GraphicsEnvironment.isHeadless());
            }

            if (gui) {
                startGUI();
            } else {
                startConsole();
            }
        }
    }

    private static Map<Option, Object> parse(String[] args) {
        List<String> list = new ArrayList<>(Arrays.asList(args));
        Map<Option, Object> map = new HashMap<>();
        map.put(Option.HELP, list.contains("-h") || list.contains("--help"));
        map.put(Option.CONSOLE, list.contains("-c") || list.contains("--console"));
        map.put(Option.GUI, list.contains("-g") || list.contains("--gui"));
        map.put(Option.VERSION, list.contains("-V") || list.contains("--version"));

        map.put(Option.CMD_LINE, parseCommandArgument(list));

        list.removeAll(Arrays.asList("-h", "--help", "-c", "--console", "-g", "--gui", "-V", "--version", "--command"));

        if (!list.isEmpty()) {
            map.put(Option.HELP, true);
        }
        return map;
    }

    private static void startConsole() {
        final Configuration configuration = new DefaultConfiguration();
        Console console = new DefaultConsole(configuration);

        final WebService webService = new DefaultWebService(console);
        final JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console, webService);
        final Context context = new DefaultContext(configuration, console, webService, javaScriptEngine);

        context.onClose(new Runnable() {
            @Override
            public void run() {
                try {
                    webService.close();
                } finally {
                    configuration.close();
                }
            }
        });

        Shutdown.addShutdownHook(new Runnable() {
            @Override
            public void run() {
                context.close();
            }
        });

        new Shell(context).start();
    }

    private static void startGUI() {
        ConsoleWindow.initialize();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ReflectiveOperationException e) {
                    LOGGER.warn("Could not set System Look and Feel!", e);
                } catch (UnsupportedLookAndFeelException e) {
                    LOGGER.warn("Could not set System Look and Feel!", e);
                } catch (RuntimeException e) {
                    LOGGER.warn("Could not set System Look and Feel!", e);
                }

                openNewWindow();
            }
        });
    }

    private static void openNewWindow() {
        openNewWindow(null);
    }

    private static void openNewWindow(Window oldWindow) {
        final Configuration configuration = new DefaultConfiguration();
        final ConsoleWindow window = new ConsoleWindow(configuration);

        if (oldWindow != null) {
            int offset = (int) (window.getWidth() * 0.1);
            window.setLocation(oldWindow.getX() + offset, oldWindow.getY() + offset);
        }

        window.setNewWindowListener(new Runnable() {
            @Override
            public void run() {
                openNewWindow(window);
            }
        });

        final Console console = window.getConsole();
        final WebService webService = new DefaultWebService(console);
        final JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console, webService);
        final Context context = new DefaultContext(configuration, console, webService, javaScriptEngine);

        context.onClose(new Runnable() {
            @Override
            public void run() {
                try {
                    webService.close();
                } finally {
                    configuration.close();
                }
            }
        });

        Shutdown.addShutdownHook(new Runnable() {
            @Override
            public void run() {
                context.close();
            }
        });

        final Thread shellThread = new Thread(new Runnable() {
            @Override
            public void run() {
                new Shell(context).start();

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        window.setVisible(false);
                        window.dispose();
                    }
                });
            }
        });

        shellThread.setName("Shell-" + SHELL_THREAD_INDEX.incrementAndGet());
        shellThread.setDaemon(true);
        shellThread.start();

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent evt) {
                try {
                    shellThread.interrupt();
                } finally {
                    context.close();
                }
            }
        });

        window.setVisible(true);
    }

    private static String parseCommandArgument(List<String> list) {
        String line = null;
        int commandIndex = list.indexOf("--command") + 1;
        if (commandIndex > 0 && (list.size() > commandIndex) ) {
            line = list.remove(commandIndex);
        }
        return line;
    }


    private static void executeCommand(String line) {
        final Configuration configuration = new DefaultConfiguration();
        Console console = new DefaultConsole(configuration);

        final WebService webService = new DefaultWebService(console);
        final JavaScriptEngine javaScriptEngine = new DefaultJavaScriptEngine(console, webService);
        final Context context = new DefaultContext(configuration, console, webService, javaScriptEngine);

        new Shell(context).execute(line);
        try {
            webService.close();
        } finally {

            try {
                configuration.close();
            } finally {
                context.close();
            }
        }
    }
}
