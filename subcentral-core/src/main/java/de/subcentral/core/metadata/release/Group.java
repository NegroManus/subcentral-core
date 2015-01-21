package de.subcentral.core.metadata.release;

import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.Settings;

/**
 * 
 * @implSpec #immutable #thread-safe
 */
public class Group implements Comparable<Group>
{
	private final String	name;

	public Group(String name)
	{
		this.name = Objects.requireNonNull(name, "name");
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
