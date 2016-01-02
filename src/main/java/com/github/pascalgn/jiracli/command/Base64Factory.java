package com.github.pascalgn.jiracli.command;

import java.util.List;

public class Base64Factory implements CommandFactory {
	@Override
	public String getName() {
		return "base64";
	}

	@Override
	public Base64 createCommand(List<String> arguments) {
		if (arguments.isEmpty()) {
			return new Base64();
		} else {
			throw new IllegalArgumentException("Invalid arguments: " + arguments);
		}
	}
}
