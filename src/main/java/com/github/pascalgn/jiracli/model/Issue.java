package com.github.pascalgn.jiracli.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Issue extends Data<IssueType> {
	private static final Pattern KEY_PATTERN = Pattern.compile("[A-Z]+-[0-9]+");

	public static Pattern getKeyPattern() {
		return KEY_PATTERN;
	}
	
	public static boolean isKey(String str) {
		return KEY_PATTERN.matcher(str).matches();
	}
	
	public static Issue valueOf(String key) {
		if (!isKey(key)) {
			throw new IllegalArgumentException("Invalid issue key: " + key);
		}
		return new Issue(key);
	}
	
	public static Issue valueOfOrNull(String str) {
		if (isKey(str)) {
			return new Issue(str);
		} else {
			return null;
		}
	}
	
	public static List<Issue> findAll(String str) {
		List<Issue> result = null;
		Matcher m = KEY_PATTERN.matcher(str);
		if (m.find()) {
			result = new ArrayList<Issue>();
			result.add(new Issue(m.group()));
		} else {
			return Collections.emptyList();
		}
		while (m.find()) {
			result.add(new Issue(m.group()));
		}
		return result;
	}
	
	private final String key;

	private Issue(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}

	@Override
	public IssueType getType() {
		return IssueType.getInstance();
	}

	@Override
	public <S extends Type> Data<S> convertTo(S target) {
		return target.accept(new DataConverter() {
			@Override
			public Issue visit(IssueType issue) {
				return Issue.this;
			}

			@Override
			public IssueList visit(IssueListType issueList) {
				return new IssueList(Collections.singleton(Issue.this).iterator());
			}
		});
	}

	@Override
	public String toString() {
		return "Issue[" + key + "]";
	}
}
