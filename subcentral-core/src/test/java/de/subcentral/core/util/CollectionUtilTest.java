package de.subcentral.core.util;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class CollectionUtilTest
{
	@Test
	public void testUpdateSortedListNaturalOrder()
	{
		// Input
		List<Integer> origList = CollectionUtil.createArrayList(1, 2, 3, 4, 6);
		List<Integer> updateList = ImmutableList.of(1, 2, 3, 4, 5, 6);
		// Apply
		CollectionUtil.updateSortedList(origList, updateList);
		// Assert
		assertEquals(updateList, origList);
	}
}
