package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

public class MappingMatcher<K>
{
	private final Pattern			pattern;
	private final Map<Integer, K>	groups;

	public MappingMatcher(Pattern pattern, Map<Integer, K> groups)
	{
		this.pattern = pattern;
		this.groups = ImmutableMap.copyOf(groups);
	}

	public Pattern getPattern()
	{
		return pattern;
	}

	/**
	 * A Map:
	 * <ul>
	 * <li>Key: group number (0: whole match string, 1-n: pattern groups)</li>
	 * <li>Value: group key</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Map<Integer, K> getGroups()
	{
		return groups;
	}

	/**
	 * 
	 * @param input
	 * @return The mapped groups. If the matcher does no match, null is returned.
	 * @throws IndexOutOfBoundsException
	 *             If there is no pattern group for a specified group number.
	 */
	public Map<K, String> match(String input) throws IndexOutOfBoundsException
	{
		if (StringUtils.isBlank(input))
		{
			return null;
		}
		Matcher m = pattern.matcher(input);
		if (m.matches())
		{
			Map<K, String> mappedGroups = new HashMap<>(groups.size());
			for (Map.Entry<Integer, K> entry : groups.entrySet())
			{
				K groupKey = entry.getValue();
				String groupValue = m.group(entry.getKey());
				// concat the values if multiple groups have the same key
				String storedValue = mappedGroups.get(groupKey);
				if (storedValue != null)
				{
					groupValue = storedValue + (groupValue != null ? " " + groupValue : "");
				}
				mappedGroups.put(groupKey, groupValue);
			}
			return mappedGroups;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this).omitNullValues().add("pattern", pattern).add("groups", groups).toString();
	}
}
