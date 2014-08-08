package de.subcentral.core.standardizing;

import java.util.function.Consumer;

public interface StandardizingService extends Consumer<Object>
{
	public <T> T standardize(T entity);

	@Override
	public default void accept(Object entity)
	{
		standardize(entity);
	}
}
