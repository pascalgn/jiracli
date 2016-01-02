package com.github.pascalgn.jiracli.model;

public final class NoneType extends Type {
	private static final NoneType INSTANCE = new NoneType();
	
	public static NoneType getInstance() {
		return INSTANCE;
	}
	
	private NoneType() {
		// only allow a single instance
	}
	
	@Override
	public <T> T accept(TypeVisitor<T> visitor) {
		return visitor.visit(this);
	}
}
