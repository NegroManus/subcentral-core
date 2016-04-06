package de.subcentral.core;

import java.util.Comparator;

import com.google.common.collect.Ordering;

public class Constants
{
	public static final Ordering<String> STRING_ORDERING = createDefaultOrdering(String.CASE_INSENSITIVE_ORDER);

	private Constants()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static <T extends Comparable<T>> Ordering<T> createDefaultOrdering()
	{
		return Ordering.natural().nullsFirst();
	}

	public static <T> Ordering<T> createDefaultOrdering(Comparator<T> comparator)
	{
		return Ordering.from(comparator).nullsFirst();
	}
}
