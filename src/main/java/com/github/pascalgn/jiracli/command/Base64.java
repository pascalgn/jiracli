package com.github.pascalgn.jiracli.command;

import java.nio.charset.StandardCharsets;

import javax.xml.bind.DatatypeConverter;

import com.github.pascalgn.jiracli.console.Console;
import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.NoneType;

class Base64 implements Command<NoneType, None, None> {
	@Override
	public NoneType getInputType() {
		return NoneType.getInstance();
	}

	@Override
	public None execute(Context context, None input) {
		Console console = context.getConsole();
		String raw;
		while ((raw = console.readLine()) != null) {
			String line = raw.trim();
			if (line.isEmpty()) {
				break;
			}
			String base64 = DatatypeConverter.printBase64Binary(line.getBytes(StandardCharsets.UTF_8));
			console.println(base64);
		}
		return None.getInstance();
	}
}
