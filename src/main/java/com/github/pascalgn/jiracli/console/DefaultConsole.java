package com.github.pascalgn.jiracli.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DefaultConsole implements Console {
	private final BufferedReader reader;

	public DefaultConsole() {
		reader = new BufferedReader(new InputStreamReader(System.in));
	}

	@Override
	public void print(String str) {
		System.out.print(str);
	}

	@Override
	public void println(String str) {
		System.out.println(str);
	}

	@Override
	public String readLine() {
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new IllegalStateException("Error reading line!", e);
		}
	}
}
