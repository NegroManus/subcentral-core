package de.subcentral.watcher.model;

import java.util.Objects;

public class ObservableBeanWrapper<T> extends ObservableBean
{
	protected final T	bean;

	public ObservableBeanWrapper(T bean)
	{
		this.bean = Objects.requireNonNull(bean, "bean");
	}

	public T getBean()
	{
		return bean;
	}
}
