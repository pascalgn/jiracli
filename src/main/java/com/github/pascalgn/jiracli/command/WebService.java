package com.github.pascalgn.jiracli.command;

import java.util.Map;

import org.json.JSONObject;

public interface WebService extends AutoCloseable {
	JSONObject getIssue(String issue);
	
	Map<String, String> getFieldMapping();
	
	@Override
	void close();
}
