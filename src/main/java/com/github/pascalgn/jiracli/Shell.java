package com.github.pascalgn.jiracli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pascalgn.jiracli.command.Base64Factory;
import com.github.pascalgn.jiracli.command.Command;
import com.github.pascalgn.jiracli.command.CommandFactory;
import com.github.pascalgn.jiracli.command.Context;
import com.github.pascalgn.jiracli.command.PrintFactory;
import com.github.pascalgn.jiracli.command.ReadExcelFactory;
import com.github.pascalgn.jiracli.command.ReadFactory;
import com.github.pascalgn.jiracli.model.Data;

class Shell {
	private static final Logger LOGGER = LoggerFactory.getLogger(Shell.class);

	private static final String PROMPT = "> ";
	private static final String EXIT = "exit";

	private static final List<CommandFactory> COMMAND_FACTORIES = Arrays.asList(new ReadFactory(),
			new ReadExcelFactory(), new PrintFactory(), new Base64Factory());

	public static CommandFactory getCommandFactory(String commandName) {
		for (CommandFactory commandFactory : COMMAND_FACTORIES) {
			if (commandFactory.getName().equals(commandName)) {
				return commandFactory;
			}
		}
		throw new IllegalArgumentException("Unknown command: " + commandName);
	}

	private final Context context;

	public Shell(Context context) {
		this.context = context;
	}

	@SuppressWarnings("unchecked")
	public void start() throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.print(PROMPT.getBytes());
			System.out.flush();

			String raw = reader.readLine();
			if (raw == null) {
				break;
			}

			String line = raw.trim();

			if (line.equals(EXIT)) {
				break;
			}

			try {
				Pipeline.Builder<Data<?>> pipelineBuilder = new Pipeline.Builder<>();
				String[] commandNames = line.trim().split("\\s*\\|\\s*");
				for (String commandName : commandNames) {
					String[] arr = commandName.split(" ");
					CommandFactory commandFactory = getCommandFactory(arr[0]);
					Command<?, ?, ?> command = commandFactory.createCommand(toList(arr, 1, arr.length - 1));
					pipelineBuilder.add((Command<?, Data<?>, Data<?>>) command);
				}
				Pipeline<Data<?>> pipeline = pipelineBuilder.build();
				pipeline.execute(context);
			} catch (RuntimeException e) {
				LOGGER.trace("Error", e);
				System.err.println("Error: " + e.getLocalizedMessage());
			}
		}
	}

	private static <T> List<T> toList(T[] arr, int offset, int length) {
		List<T> result = new ArrayList<T>(length);
		for (int i = offset; i < offset + length; i++) {
			result.add(arr[i]);
		}
		return result;
	}
}
