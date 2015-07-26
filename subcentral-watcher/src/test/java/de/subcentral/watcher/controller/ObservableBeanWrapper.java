package de.subcentral.watcher.controller;

import java.util.Objects;

import de.subcentral.fx.ObservableObject;

public class ObservableBeanWrapper<T> extends ObservableObject
{
	protected final T bean;

	public ObservableBeanWrapper(T bean)
	{
		this.bean = Objects.requireNonNull(bean, "bean");
	}

	public T getBean()
	{
		return bean;
	}
}
