package de.subcentral.core.model.release;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.Settings;

public class Group implements Comparable<Group>
{
	private final String	name;

	public Group(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && getClass().equals(obj.getClass()))
		{
			return StringUtils.equalsIgnoreCase(name, ((Group) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(31, 97).append(StringUtils.lowerCase(name)).toHashCode();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Group o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}
}
