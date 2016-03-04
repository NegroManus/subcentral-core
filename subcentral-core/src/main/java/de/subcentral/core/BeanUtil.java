package de.subcentral.core;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Multimap;

public class BeanUtil
{
	private BeanUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
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
}
