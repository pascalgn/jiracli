package com.github.pascalgn.jiracli;

import java.util.ArrayList;
import java.util.List;

import com.github.pascalgn.jiracli.command.Command;
import com.github.pascalgn.jiracli.command.Context;
import com.github.pascalgn.jiracli.model.Data;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.Type;

class Pipeline<D extends Data<?>> {
	public static final class Builder<D extends Data<?>> {
		private final List<Command<?, ?, ?>> commands;
		
		public static Builder<None> newInstance() {
			return new Builder<None>();
		}
		
		public Builder() {
			this.commands = new ArrayList<>();
		}
		
		@SuppressWarnings("unchecked")
		public <R extends Data<?>> Builder<R> add(Command<?, D, R> command) {
			commands.add(command);
			return (Builder<R>) this;
		}
		
		public Pipeline<D> build() {
			return new Pipeline<>(commands);
		}
	}
	
	private final List<Command<?, ?, ?>> commands;

	private Pipeline(List<Command<?, ?, ?>> commands) {
		this.commands = commands;
	}

	@SuppressWarnings("unchecked")
	public D execute(Context context) {
		Data<?> result = None.getInstance();
		for (Command<?, ?, ?> command : commands) {
			Type inputType = command.getInputType();
			Data<?> input = result.convertTo(inputType);
			if (input == null) {
				throw new IllegalStateException("Cannot convert data to " + inputType + ": " + result);
			}
			result = ((Command<?, Data<?>, Data<?>>) command).execute(context, input);
		}
		return (D) result;
	}
}
