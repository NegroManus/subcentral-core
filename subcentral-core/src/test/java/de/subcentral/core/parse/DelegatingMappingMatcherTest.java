package de.subcentral.core.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import de.subcentral.core.parse.DelegatingMappingMatcher.GroupEntry;
import de.subcentral.core.util.SimplePropDescriptor;

public class DelegatingMappingMatcherTest
{
	@Test
	public void testDelegatingMappingMatcher()
	{
		// Input data
		String s1 = "A-x.y-O";
		String s2 = "A-x:y-O";

		SimplePropDescriptor startProp = new SimplePropDescriptor(getClass(), "start");
		SimplePropDescriptor middle1Prop = new SimplePropDescriptor(getClass(), "middle1");
		SimplePropDescriptor middle2Prop = new SimplePropDescriptor(getClass(), "middle2");
		SimplePropDescriptor endProp = new SimplePropDescriptor(getClass(), "end");

		// Expected output data
		Map<SimplePropDescriptor, String> expected = new HashMap<>();
		expected.put(startProp, "A");
		expected.put(middle1Prop, "x");
		expected.put(middle2Prop, "y");
		expected.put(endProp, "O");

		// Configuration
		Map<Integer, SimplePropDescriptor> middleGroups = new HashMap<>();
		middleGroups.put(1, middle1Prop);
		middleGroups.put(2, middle2Prop);
		MappingMatcher<SimplePropDescriptor> middleMatcher1 = new SimpleMappingMatcher<>(Pattern.compile("(\\w):(\\w)"), middleGroups);
		MappingMatcher<SimplePropDescriptor> middleMatcher2 = new SimpleMappingMatcher<>(Pattern.compile("(\\w)\\.(\\w)"), middleGroups);

		Map<Integer, GroupEntry<SimplePropDescriptor>> groups = new HashMap<>();
		groups.put(1, GroupEntry.ofKey(startProp));
		groups.put(2, GroupEntry.ofMatcher(new MultiMappingMatcher<>(middleMatcher1, middleMatcher2)));
		groups.put(3, GroupEntry.ofKey(endProp));
		MappingMatcher<SimplePropDescriptor> matcher = new DelegatingMappingMatcher<>(Pattern.compile("(\\w+)-(.*)-(\\w+)"), groups);

		// Execution
		Map<SimplePropDescriptor, String> actual = matcher.match(s1);
		// Comparison
		Assert.assertEquals(expected, actual);

		// Execution
		actual = matcher.match(s2);
		// Comparison
		Assert.assertEquals(expected, actual);
	}
}
