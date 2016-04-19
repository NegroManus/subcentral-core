package de.subcentral.core.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

public class ObjectUtil
{
	private static final Ordering<String> DEFAULT_STRING_ORDERING = getDefaultOrdering(String.CASE_INSENSITIVE_ORDER);

	private ObjectUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static boolean equalPatterns(Pattern p1, Pattern p2)
	{
		return (p1 == p2) || (p1 != null && p1.pattern().equals(p2.pattern()) && p1.flags() == p2.flags());
	}

	public static <E> Collection<E> nullIfEmpty(Collection<E> c)
	{
		return c == null || c.isEmpty() ? null : c;
	}

	public static <K, V> Map<K, V> nullIfEmpty(Map<K, V> m)
	{
		return m == null || m.isEmpty() ? null : m;
	}

	public static <K, V> Multimap<K, V> nullIfEmpty(Multimap<K, V> m)
	{
		return m == null || m.isEmpty() ? null : m;
	}

	public static Integer nullIfZero(int num)
	{
		return num == 0 ? null : Integer.valueOf(num);
	}

	public static Long nullIfZero(long num)
	{
		return num == 0L ? null : Long.valueOf(num);
	}

	public static <T extends Comparable<T>> Ordering<T> getDefaultOrdering()
	{
		return Ordering.natural().nullsFirst();
	}

	public static <T> Ordering<T> getDefaultOrdering(Comparator<T> comparator)
	{
		return Ordering.from(comparator).nullsFirst();
	}

	public static Ordering<String> getDefaultStringOrdering()
	{
		return DEFAULT_STRING_ORDERING;
	}
}
