package de.subcentral.core;

import java.util.Comparator;

import com.google.common.collect.Ordering;

/**
 * 
 * TODO: make all Namers and Parsers immutable (safe to use concurrently and faster).
 *
 */
public class Settings
{
	// nulls first
	public static final Ordering<String>	STRING_ORDERING	= Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsFirst();

	public static <T extends Comparable<T>> Ordering<T> createDefaultOrdering()
	{
		return Ordering.natural().nullsFirst();
	}

	public static <T> Ordering<T> createDefaultOrdering(Comparator<T> comparator)
	{
		return Ordering.from(comparator).nullsFirst();
	}

	private Settings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
