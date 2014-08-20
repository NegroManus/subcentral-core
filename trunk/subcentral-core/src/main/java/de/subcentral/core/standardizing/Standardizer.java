package de.subcentral.core.standardizing;

import java.util.function.Consumer;

public interface Standardizer<T> extends Consumer<T>
{
	public void standardize(T entity);

	@Override
	public default void accept(T entity)
	{
		standardize(entity);
	}
}
