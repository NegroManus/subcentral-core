package de.subcentral.core.model.release;

import de.subcentral.core.util.Settings;

public class Tag implements Comparable<Tag>
{
	public static final String	CATEGORY_SOURCE	= "SOURCE";
	public static final String	CATEGORY_FORMAT	= "FORMAT";
	public static final String	CATEGORY_META	= "META";

	private String				name;
	private String				category;
	private String				info;

	public Tag()
	{

	}

	public Tag(String name)
	{
		this.name = name;
	}

	public Tag(String name, String category, String info)
	{
		this.name = name;
		this.category = category;
		this.info = info;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public String getInfo()
	{
		return info;
	}

	public void setInfo(String info)
	{
		this.info = info;
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
		if (Tag.class != obj.getClass())
		{
			return false;
		}
		Tag other = (Tag) obj;
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
	public int compareTo(Tag o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}
}
