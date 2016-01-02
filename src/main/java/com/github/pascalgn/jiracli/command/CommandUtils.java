package com.github.pascalgn.jiracli.command;

import java.io.Closeable;
import java.io.IOException;

class CommandUtils {
	public static void closeUnchecked(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			throw new IllegalStateException("Exception while trying to close: " + closeable, e);
		}
	}
}
