package com.github.pascalgn.jiracli.command;

import com.github.pascalgn.jiracli.console.Console;

public class DefaultContext extends AbstractContext {
	private final Console console;
	private final WebService webService;
	
	public DefaultContext(Console console, WebService webService) {
		this.console = console;
		this.webService = webService;
	}
	
	@Override
	public Console getConsole() {
		return console;
	}

	@Override
	public WebService getWebService() {
		return webService;
	}
}
