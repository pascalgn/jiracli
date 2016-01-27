package com.github.pascalgn.jiracli.testutil;

import java.util.Map;

import org.json.JSONObject;

import com.github.pascalgn.jiracli.command.WebService;

public class MockWebService implements WebService {
	@Override
	public JSONObject getIssue(String issue) {
		return null;
	}

	@Override
	public Map<String, String> getFieldMapping() {
		return null;
	}

	@Override
	public void close() {
	}
}
