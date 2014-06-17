package de.subcentral.core.util;

import java.time.temporal.Temporal;
import java.util.Comparator;

import com.google.common.collect.Ordering;

public class Settings
{
	public static final Ordering<String>	STRING_ORDERING		= Ordering.from(String.CASE_INSENSITIVE_ORDER).nullsLast();
	public static final Ordering<Temporal>	TEMPORAL_ORDERING	= Ordering.from(new TemporalComparator()).nullsLast();

	public static <T extends Comparable<T>> Ordering<T> createDefaultOrdering()
	{
		return Ordering.natural().nullsLast();
	}

	public static <T> Ordering<T> createDefaultOrdering(Comparator<T> comparator)
	{
		return Ordering.from(comparator).nullsLast();
	}

	private Settings()
	{
		// TODO Auto-generated constructor stub
	}
}
