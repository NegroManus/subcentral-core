package de.subcentral.core.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import com.google.common.collect.Ordering;

public class IterableComparator<T> implements Comparator<Iterable<T>>, Serializable
{
	private static final long	serialVersionUID	= 6378870942944289614L;

	public static final <T> IterableComparator<T> create(Comparator<T> elementComparator)
	{
		// if elem1 or elem2 is null, then the corresponding Iterable is shorter
		// and if all values before were equal, the shorter Iterable is considered less.
		// Therefore, "nullsFirst()".
		return new IterableComparator<T>(Ordering.from(Objects.requireNonNull(elementComparator, "elementComparator")).nullsFirst());
	}

	public static final <T extends Comparable<T>> IterableComparator<T> create()
	{
		return new IterableComparator<T>(Ordering.natural().nullsFirst());
	}

	private final Comparator<T>	elementComparator;

	private IterableComparator(Comparator<T> elementComparator)
	{
		this.elementComparator = elementComparator;
	}

	@Override
	public int compare(Iterable<T> iterable1, Iterable<T> iterable2)
	{
		Iterator<T> iter1 = iterable1.iterator();
		Iterator<T> iter2 = iterable2.iterator();
		int result = 0;
		while (true)
		{
			if (!iter1.hasNext())
			{
				return iter2.hasNext() ? -1 : 0;
			}
			if (!iter2.hasNext())
			{
				return 1;
			}
			result = elementComparator.compare(iter1.next(), iter2.next());
			if (result != 0)
			{
				return result;
			}
		}
	}
}
