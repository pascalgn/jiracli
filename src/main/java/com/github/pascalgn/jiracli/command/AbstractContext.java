package com.github.pascalgn.jiracli.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractContext implements Context {
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContext.class);
	
	private final List<Runnable> onClose;
	
	public AbstractContext() {
		onClose = new ArrayList<Runnable>();
	}
	
	@Override
	public void onClose(Runnable runnable) {
		Objects.requireNonNull(runnable);
		onClose.add(runnable);
	}
	
	@Override
	public void close() {
		for (Runnable runnable : onClose) {
			try {
				runnable.run();
			} catch (RuntimeException e) {
				LOGGER.info("Exception while executing onClose action!", e);
			}
		}
	}
}
