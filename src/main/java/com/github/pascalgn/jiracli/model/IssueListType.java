package com.github.pascalgn.jiracli.model;

public final class IssueListType extends Type {
	private static final IssueListType INSTANCE = new IssueListType();
	
	public static IssueListType getInstance() {
		return INSTANCE;
	}
	
	private IssueListType() {
		// only allow a single instance
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visit(this);
	}
}