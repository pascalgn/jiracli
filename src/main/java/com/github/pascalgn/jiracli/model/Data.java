package com.github.pascalgn.jiracli.model;

public abstract class Data<T extends Type> {
	public abstract T getType();
	
	public abstract <S extends Type> Data<S> convertTo(S target);
}
