package com.github.pascalgn.jiracli.model;

import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Supplier;

public class IssueList extends List<IssueListType, Issue> {
	public IssueList() {
		super();
	}

	public IssueList(Iterator<Issue> iterator) {
		super(iterator);
	}
	
	public IssueList(Supplier<Issue> supplier) {
		super(supplier);
	}
	
	@Override
	public IssueListType getType() {
		return IssueListType.getInstance();
	}

	@Override
	public <S extends Type> Data<S> convertTo(S target) {
		return target.accept(new DataConverter() {
			@Override
			public Issue visit(IssueType issue) {
				return null;
			}
			
			@Override
			public IssueList visit(IssueListType issueList) {
				return IssueList.this;
			}
		});
	}
}
