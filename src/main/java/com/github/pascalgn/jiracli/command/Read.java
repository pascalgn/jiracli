package com.github.pascalgn.jiracli.command;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Supplier;

import com.github.pascalgn.jiracli.model.Issue;
import com.github.pascalgn.jiracli.model.IssueList;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.NoneType;

class Read implements Command<NoneType, None, IssueList> {
	private static final String STDIN_FILENAME = "-";

	private final String filename;

	public Read() {
		this(STDIN_FILENAME);
	}

	public Read(String filename) {
		this.filename = filename;
	}

	@Override
	public NoneType getInputType() {
		return NoneType.getInstance();
	}

	@Override
	public IssueList execute(Context context, None input) {
		return new IssueList(getSupplier(context));
	}

	private Supplier<Issue> getSupplier(Context context) {
		if (filename.equals(STDIN_FILENAME)) {
			return new TextReader(context, filename);
		} else if (filename.toLowerCase().endsWith(".xlsx")) {
			return null;
		} else if (filename.toLowerCase().endsWith(".txt")) {
			return new TextReader(context, filename);
		} else {
			return new TextReader(context, filename);
		}
	}

	private static class TextReader implements Supplier<Issue> {
		private final Context context;
		private final String filename;

		private transient BufferedReader bufferedReader;

		public TextReader(Context context, String filename) {
			this.context = context;
			this.filename = filename;
		}

		@Override
		public Issue get() {
			String line;
			try {
				line = getBufferedReader().readLine();
			} catch (IOException e) {
				throw new IllegalStateException("Error reading from " + filename, e);
			}
			if (line == null) {
				return null;
			}
			return Issue.valueOfOrNull(line.trim());
		}

		private synchronized BufferedReader getBufferedReader() {
			if (bufferedReader == null) {
				InputStream inputStream;
				if (filename.equals(STDIN_FILENAME)) {
					inputStream = System.in;
				} else {
					try {
						inputStream = new FileInputStream(filename);
					} catch (FileNotFoundException e) {
						throw new IllegalStateException("File not found: " + filename);
					}
					context.onClose(() -> CommandUtils.closeUnchecked(inputStream));
				}
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			}
			return bufferedReader;
		}
	}
}
