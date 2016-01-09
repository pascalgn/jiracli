package com.github.pascalgn.jiracli.testutil;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TemporaryFile implements Closeable {
	private final File file;

	public TemporaryFile() throws IOException {
		this("temp", ".txt");
	}

	public TemporaryFile(String prefix, String suffix) throws IOException {
		file = File.createTempFile(prefix, suffix);
	}

	public File getFile() {
		return file;
	}

	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}
	
	public OutputStream createOutputStream() throws FileNotFoundException {
		return new FileOutputStream(file);
	}

	@Override
	public void close() throws IOException {
		if (!file.delete() && file.exists()) {
			throw new IOException("Cannot delete file: " + file);
		}
	}
}
