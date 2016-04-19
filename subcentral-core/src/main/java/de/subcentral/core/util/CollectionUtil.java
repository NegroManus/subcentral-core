package de.subcentral.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class CollectionUtil
{
	private CollectionUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	@SafeVarargs
	public static <E> List<E> createArrayList(E... items)
	{
		return new ArrayList<>(Arrays.asList(items));
	}

	public static <E> void updateList(List<E> origList, List<E> updateList)
	{
		updateList(origList, updateList, true, true, true, Objects::equals);
	}

	public static <E> void updateList(List<E> origList, List<E> updateList, boolean add, boolean replace, boolean remove)
	{
		updateList(origList, updateList, add, replace, remove, Objects::equals);
	}

	public static <E> void updateList(List<E> origList, List<E> updateList, boolean add, boolean replace, boolean remove, BiPredicate<? super E, ? super E> comparer)
	{
		updateList(origList, updateList, add, replace, remove, comparer, (List<E> list, List<E> itemsToAdd) -> origList.addAll(itemsToAdd));
	}

	public static <E extends Comparable<E>> void updateSortedList(List<E> origList, List<E> updateList)
	{
		updateSortedList(origList, updateList, true, true, true, ObjectUtil.getDefaultOrdering());
	}

	public static <E extends Comparable<E>> void updateSortedList(List<E> origList, List<E> updateList, boolean add, boolean replace, boolean remove)
	{
		updateSortedList(origList, updateList, add, replace, remove, ObjectUtil.getDefaultOrdering());
	}

	public static <E> void updateSortedList(List<E> origList, List<E> updateList, boolean add, boolean replace, boolean remove, Comparator<? super E> comparator)
	{
		updateList(origList, updateList, add, replace, remove, (E e1, E e2) -> comparator.compare(e1, e2) == 0, (List<E> list, List<E> itemsToAdd) -> addToSortedList(list, itemsToAdd, comparator));
	}

	private static <E> void addToSortedList(List<E> list, Collection<E> itemsToAdd, Comparator<? super E> comparator)
	{
		for (E item : itemsToAdd)
		{
			addToSortedList(list, item, comparator);
		}
	}

	private static <E> void addToSortedList(List<E> list, E item, Comparator<? super E> comparator)
	{
		int addIndex = list.size();
		for (int i = 0; i < list.size(); i++)
		{
			E orig = list.get(i);
			int result = comparator.compare(orig, item);
			if (result < 0)
			{
				continue;
			}
			else if (result > 0)
			{
				addIndex = i;
				break;
			}
			else
			{
				addIndex = i + 1;
				break;
			}
		}
		list.add(addIndex, item);
	}

	private static <E> void updateList(List<E> origList,
			List<E> updateList,
			boolean add,
			boolean replace,
			boolean remove,
			BiPredicate<? super E, ? super E> comparer,
			BiConsumer<List<E>, List<E>> adder)
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
			adder.accept(origList, itemsToAdd);
		}
	}
}
