package de.subcentral.core.standardizing;

import java.util.function.UnaryOperator;

public interface Standardizer<T> extends UnaryOperator<T>
{
	public T standardize(T entity);

	@Override
	public default T apply(T entity)
	{
		return standardize(entity);
	}
}
