package com.github.pascalgn.jiracli.model;

public interface TypeVisitor<T> {
	T visit(IssueType issue);
	
	T visit(IssueListType issueList);
	
	T visit(NoneType none);
}
