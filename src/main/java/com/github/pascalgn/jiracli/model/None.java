package com.github.pascalgn.jiracli.model;

public class None extends Data<NoneType> {
	private static final None INSTANCE = new None();

	public static None getInstance() {
		return INSTANCE;
	}

	private None() {
		// only allow one instance
	}
	
	@Override
	public NoneType getType() {
		return NoneType.getInstance();
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
				return null;
			}
		});
	}
}
