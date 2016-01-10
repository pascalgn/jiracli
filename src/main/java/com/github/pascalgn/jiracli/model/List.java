package com.github.pascalgn.jiracli.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.pascalgn.jiracli.util.Supplier;

abstract class List<T extends Type, E extends Data<?>> extends Data<T> {
	private final Supplier<E> supplier;

	public List() {
		this(new Supplier<E>() {
			@Override
			public E get() {
				return null;
			}
		});
	}

	public List(final Iterator<E> iterator) {
		this(new Supplier<E>() {
			@Override
			public E get() {
				return iterator.hasNext() ? iterator.next() : null;
			}
		});
	}

	public List(Supplier<E> supplier) {
		this.supplier = supplier;
	}

	public E next() {
		return supplier.get();
	}
	
	public java.util.List<E> remaining() {
		java.util.List<E> result = new ArrayList<E>();
		E item;
		while ((item = next()) != null) {
			result.add(item);
		}
		return result;
	}
}
