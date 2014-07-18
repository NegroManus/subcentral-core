package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.SimplePropDescriptor;

public class NumericGroupMappingMatcher implements MappingMatcher
{
	private final Pattern									pattern;
	private final Map<Integer, SimplePropDescriptor>	groups;

	public NumericGroupMappingMatcher(Pattern pattern, Map<Integer, SimplePropDescriptor> groups)
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
	public Map<Integer, SimplePropDescriptor> getGroups()
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
	@Override
	public Map<SimplePropDescriptor, String> map(String input) throws IndexOutOfBoundsException
	{
		Matcher m = pattern.matcher(input);
		if (m.matches())
		{
			Map<SimplePropDescriptor, String> mappedGroups = new HashMap<>(groups.size());
			for (Map.Entry<Integer, SimplePropDescriptor> entry : groups.entrySet())
			{
				SimplePropDescriptor groupName = entry.getValue();
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
