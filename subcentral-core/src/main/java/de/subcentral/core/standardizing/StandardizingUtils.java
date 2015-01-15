package de.subcentral.core.standardizing;

public class StandardizingUtils
{
	public static <T> void mayStandardize(T entity, StandardizingService standardizingService)
	{
		if (standardizingService != null)
		{
			standardizingService.standardize(entity);
		}
	}

	private StandardizingUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
