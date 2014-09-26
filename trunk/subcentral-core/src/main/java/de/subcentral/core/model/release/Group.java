package de.subcentral.core.model.release;

import java.util.Locale;

import de.subcentral.core.Settings;

public class Group implements Comparable<Group>
{
	private String	name;

	public Group()
	{

	}

	public Group(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
			Group other = (Group) obj;
			return name != null ? name.equalsIgnoreCase(other.name) : other.name == null;
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return name == null ? 0 : name.toLowerCase(Locale.getDefault()).hashCode();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Group o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}
}
