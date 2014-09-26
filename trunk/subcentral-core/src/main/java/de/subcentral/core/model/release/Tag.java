package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import de.subcentral.core.Settings;
import de.subcentral.core.util.ListComparator;

public class Tag implements Comparable<Tag>
{
	public static final Comparator<List<Tag>>	TAGS_COMPARATOR	= ListComparator.create();

	/**
	 * Tags describing the source. Like HDTV, BluRay, BDRip, DVDRip, ...
	 */
	public static final String					CATEGORY_SOURCE	= "SOURCE";

	/**
	 * Tags describing the format. Like x264, XviD, DD5.1, AC3, 720p, 1080p, (subtitle tags), (language tags), ...
	 */
	public static final String					CATEGORY_FORMAT	= "FORMAT";

	/**
	 * Tags for meta information about the release itself. Like PROPER, REPACK, READ INFO, iNTERNAL, DIRFIX, ...
	 */
	public static final String					CATEGORY_META	= "META";

	public static List<Tag> tags(Collection<String> tags)
	{
		if (tags.isEmpty())
		{
			return new ArrayList<>(0);
		}
		List<Tag> tagList = new ArrayList<>(tags.size());
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}

	public static List<Tag> tags(String... tags)
	{
		if (tags.length == 0)
		{
			return new ArrayList<>(0);
		}
		List<Tag> tagList = new ArrayList<>(tags.length);
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}

	private String	name;
	private String	longName;
	private String	category;

	public Tag()
	{

	}

	public Tag(String name)
	{
		this.name = name;
	}

	public Tag(String name, String longName, String category)
	{
		this.name = name;
		this.longName = longName;
		this.category = category;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getLongName()
	{
		return longName;
	}

	public void setLongName(String longName)
	{
		this.longName = longName;
	}

	public String getCategory()
	{
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
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
			Tag other = (Tag) obj;
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
	public int compareTo(Tag o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}
}
