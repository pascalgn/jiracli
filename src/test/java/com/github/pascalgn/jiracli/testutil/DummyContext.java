package com.github.pascalgn.jiracli.testutil;

import java.util.Map;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.AbstractContext;
import com.github.pascalgn.jiracli.command.WebService;
import com.github.pascalgn.jiracli.console.Console;
import com.github.pascalgn.jiracli.console.DefaultConsole;

public class DummyContext extends AbstractContext {
	private final Console console;
	private final WebService webService;

	public DummyContext() {
		console = new DefaultConsole();
		webService = new WebService() {
			@Override
			public JSONObject getIssue(String issue) {
				return null;
			}
			
			@Override
			public Map<String, String> getFieldMapping() {
				return null;
			}
			
			@Override
			public void close() {
			}
		};
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
