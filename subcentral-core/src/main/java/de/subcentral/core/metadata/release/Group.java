package de.subcentral.core.metadata.release;

import java.io.Serializable;

import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.ValidationUtil;

/**
 * 
 * @implSpec #immutable #thread-safe
 */
public class Group implements Comparable<Group>, Serializable
{
	private static final long	serialVersionUID	= -8704261988899599068L;

	private final String		name;

	public Group(String name) throws IllegalArgumentException
	{
		this.name = ValidationUtil.requireNotBlankAndStrip(name, "name cannot be blank");
	}

	public static Group from(String group)
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

	public static String toStringNullSafe(Group group)
	{
		return group != null ? group.toString() : "";
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
			return ObjectUtil.stringEqualIgnoreCase(name, ((Group) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return ObjectUtil.stringHashCodeIgnoreCase(name);
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Group o)
	{
		if (this == o)
		{
			return 0;
		}
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return ObjectUtil.getDefaultStringOrdering().compare(name, o.name);
	}
}
