package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.SimplePropertyDescriptor;

public class NumericGroupMappingMatcher implements MappingMatcher
{
	private final Pattern									pattern;
	private final Map<Integer, SimplePropertyDescriptor>	groups;

	public NumericGroupMappingMatcher(Pattern pattern, Map<Integer, SimplePropertyDescriptor> groups)
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
	public Map<Integer, SimplePropertyDescriptor> getGroups()
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
	public Map<SimplePropertyDescriptor, String> map(String input) throws IndexOutOfBoundsException
	{
		Matcher m = pattern.matcher(input);
		if (m.matches())
		{
			Map<SimplePropertyDescriptor, String> mappedGroups = new HashMap<>(groups.size());
			for (Map.Entry<Integer, SimplePropertyDescriptor> entry : groups.entrySet())
			{
				SimplePropertyDescriptor groupName = entry.getValue();
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
