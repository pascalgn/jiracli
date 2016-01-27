package com.github.pascalgn.jiracli.testutil;

import java.util.Arrays;
import java.util.List;

import com.github.pascalgn.jiracli.console.Console;

public class MockConsole implements Console {
	private final List<String> input;
	private final StringBuilder output;
	
	private int index;
	
	public MockConsole(String... input) {
		this.input = Arrays.asList(input);
		this.output = new StringBuilder();
	}

	public String getOutput() {
		return output.toString();
	}
	
	@Override
	public void print(String str) {
		output.append(str);
	}

	@Override
	public void println(String str) {
		output.append(str);
		output.append(System.lineSeparator());
	}

	@Override
	public String readLine() {
		return (index < input.size() ? input.get(index++) : null);
	}
}