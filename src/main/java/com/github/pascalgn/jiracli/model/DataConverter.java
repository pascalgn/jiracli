package com.github.pascalgn.jiracli.model;

abstract class DataConverter implements TypeVisitor<Data<?>> {
	@Override
	public abstract Issue visit(IssueType issue);

	@Override
	public abstract IssueList visit(IssueListType issueList);

	@Override
	public None visit(NoneType none) {
		return None.getInstance();
	}
}
