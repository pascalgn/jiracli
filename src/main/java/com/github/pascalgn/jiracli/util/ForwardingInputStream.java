package com.github.pascalgn.jiracli.util;

import java.io.IOException;
import java.io.InputStream;

public class ForwardingInputStream extends InputStream {
	private final InputStream delegate;

	public ForwardingInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	public int read() throws IOException {
		return delegate.read();
	}

	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	public String toString() {
		return delegate.toString();
	}

	public int available() throws IOException {
		return delegate.available();
	}

	public void close() throws IOException {
		delegate.close();
	}

	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	public void reset() throws IOException {
		delegate.reset();
	}

	public boolean markSupported() {
		return delegate.markSupported();
	}
}
