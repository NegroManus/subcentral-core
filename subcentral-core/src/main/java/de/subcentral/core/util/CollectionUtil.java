package de.subcentral.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiPredicate;

public class CollectionUtil
{
	private CollectionUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	public static <E> void updateList(List<E> origList, List<E> updateList, boolean add, boolean replace, boolean remove, BiPredicate<? super E, ? super E> comparer)
	{
		List<E> itemsToRemove = null;
		List<E> itemsToAdd = null;
		for (E update : updateList)
		{
			boolean origListContainsUpdate = false;
			ListIterator<E> origIter = origList.listIterator();
			while (origIter.hasNext())
			{
				E orig = origIter.next();
				if (comparer.test(orig, update))
				{
					origListContainsUpdate = true;
					if (remove)
					{
						if (itemsToRemove == null)
						{
							itemsToRemove = new ArrayList<>(origList);
						}
						itemsToRemove.remove(orig);
					}
					if (replace)
					{
						origIter.set(update);
					}
					break;
				}
			}
			if (!origListContainsUpdate && add)
			{
				if (itemsToAdd == null)
				{
					itemsToAdd = new ArrayList<>();
				}
				itemsToAdd.add(update);
			}
		}
		if (remove)
		{
			if (itemsToRemove != null)
			{
				origList.removeAll(itemsToRemove);
			}
			else
			{
				origList.clear();
			}
		}
		if (itemsToAdd != null)
		{
			origList.addAll(itemsToAdd);
		}
	}
}
