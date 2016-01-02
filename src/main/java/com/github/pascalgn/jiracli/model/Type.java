package com.github.pascalgn.jiracli.model;

public abstract class Type {
	Type() {
		// only allow subclasses in this package
	}

	public abstract <T> T accept(TypeVisitor<T> visitor);

	@SuppressWarnings("unchecked")
	<T extends Type> Data<T> accept(DataConverter dataConverter) {
		return (Data<T>) accept((TypeVisitor<?>) dataConverter);
	}
}
