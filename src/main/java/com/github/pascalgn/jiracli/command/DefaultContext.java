package com.github.pascalgn.jiracli.command;

import com.github.pascalgn.jiracli.console.Console;

public class DefaultContext implements Context {
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

	@Override
	public void onClose(Runnable runnable) {
		// TODO Auto-generated method stub
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
	}
}
