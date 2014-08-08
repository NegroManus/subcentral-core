package de.subcentral.core.standardizing;

import java.util.function.Consumer;

public interface StandardizingService extends Consumer<Object>
{
	public void standardize(Object entity);

	@Override
	public default void accept(Object entity)
	{
		standardize(entity);
	}
}
