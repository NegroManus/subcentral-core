package de.subcentral.core.util;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

public class ListComparatorTest
{
	@Test
	public void testListComparator()
	{
		Comparator<List<Integer>> c = ListComparator.create();

		List<Integer> list1 = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
		List<Integer> list2 = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6, 7 });
		assertTrue(c.compare(list1, list2) < 0);

		list1 = Arrays.asList(new Integer[] { 1, 2, 4, 4, 5, 6 });
		list2 = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
		assertTrue(c.compare(list1, list2) > 0);

		list1 = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
		list2 = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 });
		assertTrue(c.compare(list1, list2) == 0);

		list1 = new ArrayList<>(0);
		list2 = new ArrayList<>(0);
		assertTrue(c.compare(list1, list2) == 0);
	}

}
