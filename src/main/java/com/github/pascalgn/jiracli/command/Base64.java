package com.github.pascalgn.jiracli.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.github.pascalgn.jiracli.model.None;
import com.github.pascalgn.jiracli.model.NoneType;

class Base64 implements Command<NoneType, None, None> {
	@Override
	public NoneType getInputType() {
		return NoneType.getInstance();
	}

	@Override
	public None execute(Context context, None input) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			String raw;
			while ((raw = reader.readLine()) != null) {
				String line = raw.trim();
				if (line.isEmpty()) {
					break;
				}
				String base64 = java.util.Base64.getEncoder().encodeToString(line.getBytes(StandardCharsets.UTF_8));
				System.out.println(base64);
			}
		} catch (IOException e) {
			throw new IllegalStateException("Error reading input!", e);
		}
		
		return None.getInstance();
	}
}
