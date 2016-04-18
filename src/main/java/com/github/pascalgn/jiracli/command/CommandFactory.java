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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.github.pascalgn.jiracli.command.Argument.Parameters;
import com.github.pascalgn.jiracli.util.StringUtils;

public class CommandFactory {
    private static final CommandFactory INSTANCE = new CommandFactory();

    /**
     * @return The singleton instance
     */
    public static CommandFactory getInstance() {
        return INSTANCE;
    }

    public static class UsageException extends IllegalArgumentException {
        private static final long serialVersionUID = 2327985516601818756L;

        public UsageException(String message) {
            super(message);
        }
    }

    private final List<CommandDescriptor> commandDescriptors;

    private CommandFactory() {
        commandDescriptors = new ArrayList<>();
        for (Class<Command> type : CommandList.getCommands()) {
            CommandDescription commandDescription = type.getAnnotation(CommandDescription.class);
            if (commandDescription == null) {
                throw new IllegalStateException("Missing " + CommandDescription.class + " annotation: " + type);
            }

            // check if the command has a public default constructor:
            try {
                type.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Command does not have a public default constructor: " + type, e);
            }

            List<ArgumentDescriptor> argumentDescriptors = new ArrayList<>();
            for (Field field : type.getDeclaredFields()) {
                Argument argument = field.getAnnotation(Argument.class);
                if (argument == null) {
                    continue;
                }
                argumentDescriptors.add(new ArgumentDescriptor(Arrays.asList(argument.names()), field,
                        argument.description(), argument.parameters(), argument.variable(), argument.order()));
            }

            commandDescriptors.add(new CommandDescriptor(Arrays.asList(commandDescription.names()), type,
                    commandDescription.description(), argumentDescriptors));
        }
        Collections.sort(commandDescriptors);
    }

    List<CommandDescriptor> getCommandDescriptors() {
        return commandDescriptors;
    }

    public List<String> getCommandNames() {
        List<String> names = new ArrayList<String>();
        for (CommandDescriptor commandDescriptor : commandDescriptors) {
            names.addAll(commandDescriptor.getNames());
        }
        return names;
    }

    public Command parseCommand(String commandName, List<String> args) {
        CommandDescriptor commandDescriptor = getCommandDescriptor(commandName);
        if (commandDescriptor == null) {
            throw new IllegalArgumentException("Unknown command: " + commandName);
        }

        Command command;
        try {
            command = commandDescriptor.getCommand().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create instance: " + commandDescriptor.getCommand());
        }

        boolean parseArguments = true;
        List<String> mainArgs = new ArrayList<String>();

        UsageException exception = null;
        boolean help = false;

        for (int i = 0; i < args.size(); i++) {
            String arg = args.get(i);
            if (arg.equals("--")) {
                parseArguments = false;
            } else {
                if (isOption(arg)) {
                    if (parseArguments) {
                        if (arg.equals("-h") || arg.equals("--help")) {
                            help = true;
                        } else {
                            ArgumentDescriptor argument = commandDescriptor.getArgument(arg);
                            if (argument == null) {
                                exception = new UsageException(getUsage(commandDescriptor, false));
                            } else {
                                List<String> params = getParameters(argument, args, i, false);
                                if (params == null) {
                                    exception = new UsageException(commandName + ": " + arg + ": missing parameter!");
                                } else {
                                    argument.setParameter(command, params);
                                    i += params.size();
                                }
                            }
                        }
                    } else {
                        mainArgs.add(arg);
                    }
                } else {
                    mainArgs.add(arg);
                }
            }
        }

        if (help) {
            throw new UsageException(getUsage(commandDescriptor, true));
        } else if (exception != null) {
            throw exception;
        }

        List<ArgumentDescriptor> unnamedArguments = commandDescriptor.getUnnamedArguments();
        if (unnamedArguments.size() > 1) {
            Collections.sort(unnamedArguments, new ArgumentDescriptorComparator());
        }

        int mainIndex = 0;
        for (ArgumentDescriptor argument : unnamedArguments) {
            List<String> params = getParameters(argument, mainArgs, mainIndex - 1, true);
            if (params != null && !params.isEmpty()) {
                argument.setParameter(command, params);
                mainIndex += params.size();
            }
        }

        for (ArgumentDescriptor argument : unnamedArguments) {
            if (argument.getParameters() == Parameters.ONE || argument.getParameters() == Parameters.ONE_OR_MORE) {
                if (argument.isNull(command)) {
                    throw new UsageException(commandName + ": missing " + argument.getVariable());
                }
            }
        }

        if (mainIndex != mainArgs.size()) {
            throw new UsageException(commandName + ": exceeding arguments!");
        }

        return command;
    }

    private static List<String> getParameters(ArgumentDescriptor argument, List<String> args, int i, boolean main) {
        switch (argument.getParameters()) {
        case ZERO:
            return Collections.emptyList();

        case ZERO_OR_ONE:
            if (i + 1 < args.size()) {
                String arg = args.get(i + 1);
                if (!isOption(arg) || main) {
                    return Collections.singletonList(arg);
                }
            }
            return Collections.emptyList();

        case ZERO_OR_MORE:
            List<String> params0 = new ArrayList<String>();
            for (int j = i + 1; j < args.size(); j++) {
                String arg = args.get(j);
                if (isOption(arg) && !main) {
                    break;
                } else {
                    params0.add(arg);
                }
            }
            return params0;

        case ONE:
            if (i + 1 < args.size()) {
                String arg = args.get(i + 1);
                if (!isOption(arg) || main) {
                    return Collections.singletonList(arg);
                }
            }
            return null;

        case ONE_OR_MORE:
            List<String> params1 = new ArrayList<String>();
            for (int j = i + 1; j < args.size(); j++) {
                String arg = args.get(j);
                if (isOption(arg) && !main) {
                    break;
                } else {
                    params1.add(arg);
                }
            }
            return (params1.isEmpty() ? null : params1);

        default:
            throw new IllegalStateException();
        }
    }

    private static boolean isOption(String str) {
        return str.startsWith("-") && !str.equals("-");
    }

    private CommandDescriptor getCommandDescriptor(String name) {
        for (CommandDescriptor commandDescriptor : commandDescriptors) {
            if (commandDescriptor.getNames().contains(name)) {
                return commandDescriptor;
            }
        }
        return null;
    }

    private static String getUsage(CommandDescriptor commandDescriptor, boolean full) {
        StringBuilder str = new StringBuilder("usage: ");
        str.append(commandDescriptor.getLongName());
        for (ArgumentDescriptor argument : commandDescriptor.getNamedArguments()) {
            str.append(" [");
            str.append(argument.getShortName());
            appendVariable(str, argument);
            str.append("]");
        }
        str.append(" [-h]");
        boolean addedSeparator = false;
        for (ArgumentDescriptor argument : commandDescriptor.getUnnamedArguments()) {
            if (argument.getParameters() == Parameters.ZERO) {
                throw new IllegalStateException();
            }
            if (!addedSeparator) {
                str.append(" [--]");
                addedSeparator = true;
            }
            appendVariable(str, argument);
        }
        if (full) {
            str.append(System.lineSeparator());

            str.append(System.lineSeparator());

            str.append(commandDescriptor.getDescription());
            str.append(System.lineSeparator());

            str.append(System.lineSeparator());

            str.append("options:");
            str.append(System.lineSeparator());

            String help = "-h, --help";
            int maxLength = help.length();
            for (ArgumentDescriptor argument : commandDescriptor.getArguments()) {
                String name = getArgumentName(argument);
                maxLength = Math.max(maxLength, name.length());
            }

            for (ArgumentDescriptor argument : commandDescriptor.getNamedArguments()) {
                appendArgument(str, argument, maxLength);
                str.append(System.lineSeparator());
            }

            str.append("  ");
            str.append(help);
            str.append(StringUtils.repeat(" ", maxLength - help.length()));
            str.append("  ");
            str.append("show this command usage");

            for (ArgumentDescriptor argument : commandDescriptor.getUnnamedArguments()) {
                str.append(System.lineSeparator());
                appendArgument(str, argument, maxLength);
            }
        }
        return str.toString();
    }

    private static void appendArgument(StringBuilder str, ArgumentDescriptor argument, int maxLength) {
        str.append("  ");
        String name = getArgumentName(argument);
        str.append(name);
        str.append(StringUtils.repeat(" ", maxLength - name.length()));
        str.append("  ");
        str.append(argument.getDescription());
    }

    private static String getArgumentName(ArgumentDescriptor argument) {
        if (argument.getNames().isEmpty()) {
            return argument.getVariable();
        } else {
            return StringUtils.join(argument.getNames(), ", ");
        }
    }

    private static void appendVariable(StringBuilder str, ArgumentDescriptor argument) {
        switch (argument.getParameters()) {
        case ZERO:
            break;

        case ZERO_OR_ONE:
            str.append(" [");
            str.append(argument.getVariable());
            str.append("]");
            break;

        case ZERO_OR_MORE:
            str.append(" [");
            str.append(argument.getVariable());
            str.append("...]");
            break;

        case ONE:
            str.append(" ");
            str.append(argument.getVariable());
            break;

        case ONE_OR_MORE:
            str.append(" ");
            str.append(argument.getVariable());
            str.append("...");
            break;

        default:
            throw new IllegalStateException("Invalid parameters value: " + argument);
        }
    }

    static class CommandDescriptor implements Comparable<CommandDescriptor> {
        private final List<String> names;
        private final Class<Command> command;
        private final String description;
        private final List<ArgumentDescriptor> arguments;

        public CommandDescriptor(List<String> names, Class<Command> command, String description,
                List<ArgumentDescriptor> arguments) {
            if (names.isEmpty()) {
                throw new IllegalArgumentException();
            }
            this.names = names;
            this.command = command;
            this.description = description;
            this.arguments = arguments;
        }

        public List<String> getNames() {
            return names;
        }

        public String getLongName() {
            String name = null;
            for (String n : names) {
                if (name == null || n.length() > name.length()) {
                    name = n;
                }
            }
            return name;
        }

        public Class<Command> getCommand() {
            return command;
        }

        public String getDescription() {
            return description;
        }

        public List<ArgumentDescriptor> getArguments() {
            return arguments;
        }

        public ArgumentDescriptor getArgument(String name) {
            for (ArgumentDescriptor argument : arguments) {
                if (argument.getNames().contains(name)) {
                    return argument;
                }
            }
            return null;
        }

        public List<ArgumentDescriptor> getNamedArguments() {
            List<ArgumentDescriptor> named = new ArrayList<>();
            for (ArgumentDescriptor argument : arguments) {
                if (!argument.getNames().isEmpty()) {
                    named.add(argument);
                }
            }
            return named;
        }

        public List<ArgumentDescriptor> getUnnamedArguments() {
            List<ArgumentDescriptor> unnamed = new ArrayList<>();
            for (ArgumentDescriptor argument : arguments) {
                if (argument.getNames().isEmpty()) {
                    unnamed.add(argument);
                }
            }
            return unnamed;
        }

        @Override
        public int compareTo(CommandDescriptor other) {
            String name = Objects.toString(getLongName(), "");
            String otherName = Objects.toString(other.getLongName(), "");
            return name.compareTo(otherName);
        }
    }

    static class ArgumentDescriptor {
        private final List<String> names;
        private final String description;
        private final Field field;
        private final Parameters parameters;
        private final String variable;
        private final int order;

        public ArgumentDescriptor(List<String> names, Field field, String description, Parameters parameters,
                String variable, int order) {
            this.names = names;
            this.field = field;
            this.description = description;
            this.parameters = parameters;
            this.variable = variable;
            this.order = order;
        }

        public List<String> getNames() {
            return names;
        }

        public String getShortName() {
            String name = null;
            for (String n : names) {
                if (name == null || n.length() < name.length()) {
                    name = n;
                }
            }
            return name;
        }

        public String getDescription() {
            return description;
        }

        public boolean isNull(Command command) {
            field.setAccessible(true);
            try {
                return field.get(command) == null;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot get field value!", e);
            }
        }

        public void setParameter(Command command, List<String> params) {
            Object value = convertParameter(field, params);
            field.setAccessible(true);
            try {
                field.set(command, value);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Cannot set field value!", e);
            }
        }

        private static Object convertParameter(Field field, List<String> params) {
            if (field.getType() == List.class) {
                return params;
            }
            if (params.isEmpty()) {
                if (field.getType() == boolean.class) {
                    return true;
                } else if (field.getType() == Boolean.class) {
                    return Boolean.TRUE;
                }
            } else if (params.size() == 1) {
                String param = params.get(0);
                if (field.getType() == String.class) {
                    return param;
                } else if (field.getType() == Pattern.class) {
                    return Pattern.compile(param);
                } else if (field.getType() == Integer.class) {
                    return Integer.valueOf(param);
                } else if (field.getType() == int.class) {
                    return Integer.parseInt(param);
                }
            }
            throw new IllegalStateException("Cannot convert parameter: field " + field + ", parameter: " + params);
        }

        public Parameters getParameters() {
            return parameters;
        }

        public String getVariable() {
            return variable;
        }

        public int getOrder() {
            return order;
        }
    }

    /**
     * Sorts {@link ArgumentDescriptor} objects by the field <code>order</code>
     */
    private static class ArgumentDescriptorComparator implements Comparator<ArgumentDescriptor> {
        @Override
        public int compare(ArgumentDescriptor a1, ArgumentDescriptor a2) {
            return Integer.compare(a1.getOrder(), a2.getOrder());
        }
    }
}
