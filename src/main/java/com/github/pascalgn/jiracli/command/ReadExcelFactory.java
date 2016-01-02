package com.github.pascalgn.jiracli.command;

import java.util.List;

public class ReadExcelFactory implements CommandFactory {
	@Override
	public String getName() {
		return "readexcel";
	}

	@Override
	public ReadExcel createCommand(List<String> arguments) {
		if (arguments.size() == 1) {
			return new ReadExcel(arguments.get(0));
		} else {
			throw new IllegalArgumentException("Invalid arguments: " + arguments);
		}
	}
}
