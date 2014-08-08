package de.subcentral.core.util;

import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import de.subcentral.core.Settings;

public class ListComparator<T> implements Comparator<List<T>>
{
	public static <T> ListComparator<T> create(Comparator<T> comparator)
	{
		return new ListComparator<>(comparator);
	}

	public static <T extends Comparable<T>> ListComparator<T> create()
	{
		return new ListComparator<>(Ordering.natural());
	}

	private final Comparator<T>	comparator;

	private ListComparator(Comparator<T> comparator)
	{
		this.comparator = comparator;
	}

	@Override
	public int compare(List<T> o1, List<T> o2)
	{
		ComparisonChain chain = ComparisonChain.start();
		for (int i = 0; i < Math.max(o1.size(), o2.size()); i++)
		{
			T item1;
			try
			{
				item1 = o1.get(i);
			}
			catch (IndexOutOfBoundsException e)
			{
				item1 = null;
			}
			T item2;
			try
			{
				item2 = o2.get(i);
			}
			catch (IndexOutOfBoundsException e)
			{
				item2 = null;
			}
			// if item1 or item2 is null, then the corresponding list is shorter
			// and if all values before were equal, the shorter list is considered less.
			// Therefore, "nullsFirst()".
			chain.compare(item1, item2, Settings.createDefaultOrdering(comparator));

			// if one of the list has ended, no more comparison is needed
			if (item1 == null || item2 == null)
			{
				break;
			}
		}
		return chain.result();
	}
}
