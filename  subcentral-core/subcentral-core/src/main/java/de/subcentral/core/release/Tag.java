package de.subcentral.core.release;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class Tag implements Comparable<Tag>
{
	private String	name;
	private String	category;
	private String	info;

	public Tag()
	{

	}

	public Tag(String name)
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
		return name == null ? 0 : getName().hashCode();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Tag o)
	{
		return o == null ? 1 : new CompareToBuilder().append(name, o.name).toComparison();
	}
}
