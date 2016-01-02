package com.github.pascalgn.jiracli.model;

public final class IssueType extends Type {
	private static final IssueType INSTANCE = new IssueType();
	
	public static IssueType getInstance() {
		return INSTANCE;
	}
	
	private IssueType() {
		// only allow a single instance
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
