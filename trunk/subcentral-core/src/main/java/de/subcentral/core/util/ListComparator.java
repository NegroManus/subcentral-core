package de.subcentral.core.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Ordering;

public class ListComparator<T> implements Comparator<List<T>>, Serializable
{
	private static final long	serialVersionUID	= 6378870942944289614L;

	public static final <T> ListComparator<T> create(Comparator<T> comparator)
	{
		// if item1 or item2 is null, then the corresponding list is shorter
		// and if all values before were equal, the shorter list is considered less.
		// Therefore, "nullsFirst()".
		return new ListComparator<T>(Ordering.from(Objects.requireNonNull(comparator, "comparator")).nullsFirst());
	}

	public static final <T extends Comparable<T>> ListComparator<T> create()
	{
		return new ListComparator<T>(Ordering.natural().nullsFirst());
	}

	private final Ordering<T>	ordering;

	private ListComparator(Ordering<T> ordering)
	{
		this.ordering = ordering;
	}

	@Override
	public int compare(List<T> list1, List<T> list2)
	{
		if (list1.isEmpty() && list2.isEmpty())
		{
			return 0;
		}
		int result = 0;
		T item1;
		T item2;
		for (int i = 0; i < Math.max(list1.size(), list2.size()); i++)
		{
			try
			{
				item1 = list1.get(i);
			}
			catch (IndexOutOfBoundsException e)
			{
				item1 = null;
			}
			try
			{
				item2 = list2.get(i);
			}
			catch (IndexOutOfBoundsException e)
			{
				item2 = null;
			}
			result = ordering.compare(item1, item2);
			if (result != 0)
			{
				return result;
			}
		}
		return 0;
	}
}
