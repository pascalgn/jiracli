package com.github.pascalgn.jiracli.command;

import com.github.pascalgn.jiracli.console.Console;

public interface Context extends AutoCloseable {
	Console getConsole();
	
	WebService getWebService();
	
	void onClose(Runnable runnable);
	
	@Override
	void close();
}
