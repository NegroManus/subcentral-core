package de.subcentral.core.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class DelegatingMappingMatcher<K> implements MappingMatcher<K>
{
	/**
	 * This separator is used to separate multiple values for the same key. These values are concatenated, using this separator.
	 */
	public static final String							VALUES_WITH_SAME_KEY_SEPARATOR	= " ";

	private final Pattern								pattern;
	private final ImmutableMap<Integer, GroupEntry<K>>	groups;
	private final ImmutableMap<K, String>				predefinedMatches;

	public DelegatingMappingMatcher(Pattern pattern, Map<Integer, GroupEntry<K>> groups)
	{
		this(pattern, groups, ImmutableMap.of());
	}

	public DelegatingMappingMatcher(Pattern pattern, Map<Integer, GroupEntry<K>> groups, Map<K, String> predefinedMatches)
	{
		this.pattern = Objects.requireNonNull(pattern, "pattern");
		this.groups = ImmutableMap.copyOf(new TreeMap<>(groups)); // TreeMap to sort; includes null checks
		this.predefinedMatches = ImmutableMap.copyOf(predefinedMatches); // includes null checks
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	/**
	 * A Map:
	 * <ul>
	 * <li>Key: group number (0: whole match string, 1-n: capturing pattern groups)</li>
	 * <li>Value: group key</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Map<Integer, GroupEntry<K>> getGroups()
	{
		return groups;
	}

	public Map<K, String> getPredefinedMatches()
	{
		return predefinedMatches;
	}

	@Override
	public Map<K, String> match(String text) throws IndexOutOfBoundsException
	{
		if (text == null)
		{
			return ImmutableMap.of();
		}
		Matcher m = pattern.matcher(text);
		if (m.matches())
		{
			Map<K, String> mappedGroups = new HashMap<>(groups.size() + predefinedMatches.size());
			mappedGroups.putAll(predefinedMatches);
			for (Map.Entry<Integer, GroupEntry<K>> entry : groups.entrySet())
			{
				GroupEntry<K> groupEntry = entry.getValue();
				String groupValue = m.group(entry.getKey());
				// groupVal can be null for optional groups
				if (groupValue != null)
				{
					groupEntry.mergeGroupValue(mappedGroups, groupValue);
				}
			}
			return mappedGroups;
		}
		return ImmutableMap.of();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this).add("pattern", pattern).add("groups", groups).add("predefinedMatches", predefinedMatches).toString();
	}

	public static interface GroupEntry<K>
	{
		public boolean isKeyEntry();

		public void mergeGroupValue(Map<K, String> mappedGroups, String value);

		public static <K> void mergeStringValues(Map<K, String> map, K key, String value)
		{
			// concat the values if multiple values have the same key
			map.merge(key,
					value,
					(String oldVal, String newVal) -> new StringBuilder(oldVal.length() + VALUES_WITH_SAME_KEY_SEPARATOR.length() + newVal.length()).append(oldVal)
							.append(VALUES_WITH_SAME_KEY_SEPARATOR)
							.append(newVal)
							.toString());
		}
	}

	public static class KeyEntry<K> implements GroupEntry<K>
	{
		private final K key;

		public KeyEntry(K key)
		{
			this.key = Objects.requireNonNull(key, "key");
		}

		public K getKey()
		{
			return key;
		}

		@Override
		public boolean isKeyEntry()
		{
			return true;
		}

		@Override
		public void mergeGroupValue(Map<K, String> mappedGroups, String value)
		{
			GroupEntry.mergeStringValues(mappedGroups, key, value);
		}
	}

	public static class MatcherEntry<K> implements GroupEntry<K>
	{
		private final ImmutableList<MappingMatcher<K>> matchers;

		public MatcherEntry(Iterable<MappingMatcher<K>> matchers)
		{
			this.matchers = ImmutableList.copyOf(matchers); // includes null check
		}

		public ImmutableList<MappingMatcher<K>> getMatchers()
		{
			return matchers;
		}

		@Override
		public boolean isKeyEntry()
		{
			return false;
		}

		@Override
		public void mergeGroupValue(Map<K, String> mappedGroups, String value)
		{
			for (MappingMatcher<K> matcher : matchers)
			{
				Map<K, String> result = matcher.match(value);
				if (!result.isEmpty())
				{
					for (Map.Entry<K, String> entry : result.entrySet())
					{
						GroupEntry.mergeStringValues(mappedGroups, entry.getKey(), entry.getValue());
					}
					return;
				}
			}
		}
	}
}
