package com.github.pascalgn.jiracli.command;

import java.util.List;

public interface CommandFactory {
	String getName();
	
	Command<?, ?, ?> createCommand(List<String> arguments);
}
