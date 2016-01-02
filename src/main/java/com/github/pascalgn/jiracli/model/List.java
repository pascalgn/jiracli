package com.github.pascalgn.jiracli.model;

import java.util.Iterator;
import java.util.function.Supplier;

abstract class List<T extends Type, E extends Data<?>> extends Data<T> {
	private final Supplier<E> supplier;

	public List() {
		this(() -> null);
	}

	public List(Iterator<E> iterator) {
		this(() -> iterator.hasNext() ? iterator.next() : null);
	}

	public List(Supplier<E> supplier) {
		this.supplier = supplier;
	}

	public E next() {
		return supplier.get();
	}
}
