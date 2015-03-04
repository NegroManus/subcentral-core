package de.subcentral.core.standardizing;

public class StandardizingUtil
{
	public static <T> void mayStandardize(T entity, StandardizingService standardizingService)
	{
		if (standardizingService != null)
		{
			standardizingService.standardize(entity);
		}
	}

	private StandardizingUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
