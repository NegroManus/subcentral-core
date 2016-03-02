package de.subcentral.core.parse;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

public class DelegatingMappingMatcher<K> implements MappingMatcher<K>
{
	private final Pattern						pattern;
	private final Map<Integer, GroupEntry<K>>	groups;
	private final Map<K, String>				predefinedMatches;

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

	public static abstract class GroupEntry<K>
	{
		// Private constructor so that there can be no sub classes outside the DelegatingMappingMatcher class
		private GroupEntry()
		{

		}

		public static <K> KeyEntry<K> ofKey(K key)
		{
			return new KeyEntry<>(key);
		}

		public static <K> MatcherEntry<K> ofMatcher(MappingMatcher<K> matcher)
		{
			return new MatcherEntry<>(matcher);
		}

		protected abstract void mergeGroupValue(Map<K, String> mappedGroups, String value);

		protected static <K> void mergeStringValues(Map<K, String> map, K key, String value)
		{
			// concat the values if multiple values have the same key
			map.merge(key,
					value,
					(String oldVal, String newVal) -> new StringBuilder(oldVal.length() + PatternMappingMatcher.VALUES_WITH_SAME_KEY_SEPARATOR.length() + newVal.length()).append(oldVal)
							.append(PatternMappingMatcher.VALUES_WITH_SAME_KEY_SEPARATOR)
							.append(newVal)
							.toString());
		}
	}

	public static class KeyEntry<K> extends GroupEntry<K>
	{
		private final K key;

		private KeyEntry(K key)
		{
			this.key = Objects.requireNonNull(key, "key");
		}

		public K getKey()
		{
			return key;
		}

		@Override
		public void mergeGroupValue(Map<K, String> mappedGroups, String value)
		{
			GroupEntry.mergeStringValues(mappedGroups, key, value);
		}
	}

	public static class MatcherEntry<K> extends GroupEntry<K>
	{
		private final MappingMatcher<K> matcher;

		public MatcherEntry(MappingMatcher<K> matcher)
		{
			this.matcher = Objects.requireNonNull(matcher, "matcher");
		}

		public MappingMatcher<K> getMatcher()
		{
			return matcher;
		}

		@Override
		public void mergeGroupValue(Map<K, String> mappedGroups, String value)
		{
			Map<K, String> result = matcher.match(value);
			for (Map.Entry<K, String> entry : result.entrySet())
			{
				GroupEntry.mergeStringValues(mappedGroups, entry.getKey(), entry.getValue());
			}
		}
	}
}
