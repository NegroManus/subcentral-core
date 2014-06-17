package de.subcentral.core.release;

import de.subcentral.core.util.Settings;

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
		if (obj == null)
		{
			return false;
		}
		if (this == obj)
		{
			return true;
		}
		if (Group.class != obj.getClass())
		{
			return false;
		}
		Group other = (Group) obj;
		return name == null ? other.name == null : name.equalsIgnoreCase(other.name);
	}

	@Override
	public int hashCode()
	{
		return name == null ? 0 : name.toLowerCase().hashCode();
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
