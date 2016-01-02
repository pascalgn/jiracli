package com.github.pascalgn.jiracli.command;

import java.util.List;

public class PrintFactory implements CommandFactory {
	@Override
	public String getName() {
		return "print";
	}

	@Override
	public Print createCommand(List<String> arguments) {
		if (arguments.size() == 1) {
			return new Print(arguments.get(0));
		} else {
			throw new IllegalArgumentException("Invalid arguments: " + arguments);
		}
	}
}
