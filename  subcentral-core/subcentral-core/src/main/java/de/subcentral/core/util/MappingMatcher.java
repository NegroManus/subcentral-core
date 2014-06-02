package de.subcentral.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableMap;

public class MappingMatcher
{
	private final Pattern				pattern;
	private final Map<Integer, String>	groups;

	public MappingMatcher(Pattern pattern, Map<Integer, String> groups)
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
	 * <li>Value: group name</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Map<Integer, String> getGroups()
	{
		return groups;
	}

	/**
	 * 
	 * @param input
	 * @return The mapped groups. If the matcher does no match, null is returned.
	 * @throws IndexOutOfBoundsException If there is no pattern group for a specified group number.
	 */
	public Map<String, String> map(String input) throws IndexOutOfBoundsException
	{
		Matcher m = pattern.matcher(input);
		if (m.matches())
		{
			Map<String, String> mappedGroups = new HashMap<>(groups.size());
			for (Map.Entry<Integer, String> entry : groups.entrySet())
			{
				String groupName = entry.getValue();
				String groupValue = m.group(entry.getKey());
				// concat the values if multiple groups have the same name
				String storedValue = mappedGroups.get(groupName);
				if (storedValue != null)
				{
					groupValue = groupValue + storedValue;
				}
				mappedGroups.put(groupName, groupValue);
			}
			return mappedGroups;
		}
		return null;
	}

	@Override
	public String toString()
	{
		return new ToStringBuilder(this).append("pattern", pattern).append("groups", groups).build();
	}
}
