package de.subcentral.core.metadata.release;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.Settings;

/**
 * 
 * @implSpec #immutable #thread-safe
 */
public class Group implements Comparable<Group>, Serializable
{
	private static final long serialVersionUID = -8704261988899599068L;

	public static Group parse(String group)
	{
		try
		{
			return new Group(group);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public static String toSafeString(Group group)
	{
		return group == null ? "" : group.getName();
	}

	private final String name;

	public Group(String name) throws IllegalArgumentException
	{
		this.name = BeanUtil.requireNotBlankAndTrimWhitespace(name, "name cannot be blank");
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
		if (obj instanceof Group)
		{
			return name.equalsIgnoreCase(((Group) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(31, 97).append(name.toLowerCase(Locale.ENGLISH)).toHashCode();
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
