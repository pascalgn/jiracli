package com.github.pascalgn.jiracli.command;

import java.util.List;

public class ReadFactory implements CommandFactory {
	@Override
	public String getName() {
		return "read";
	}

	@Override
	public Read createCommand(List<String> arguments) {
		if (arguments.size() == 0) {
			return new Read();
		} else if (arguments.size() == 1) {
			return new Read(arguments.get(0));
		} else {
			throw new IllegalArgumentException("Invalid arguments: " + arguments);
		}
	}
}
