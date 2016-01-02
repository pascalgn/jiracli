package com.github.pascalgn.jiracli.command;

import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.Type;

/**
 * @param <T> Input type
 * @param <D> Input data
 * @param <R> Return data
 */
public interface Command<T extends Type, D extends Data<T>, R extends Data<?>> {
	T getInputType();
	
	R execute(Context context, D input);
}
