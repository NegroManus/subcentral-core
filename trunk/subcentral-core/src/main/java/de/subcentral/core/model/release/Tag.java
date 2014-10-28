package de.subcentral.core.model.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import de.subcentral.core.Settings;
import de.subcentral.core.util.ListComparator;

public class Tag implements Comparable<Tag>
{
	public static final Comparator<List<Tag>>	TAGS_COMPARATOR		= ListComparator.create();

	/**
	 * Tags describing the source. Like HDTV, BluRay, BDRip, DVDRip, ...
	 */
	public static final String					CATEGORY_SOURCE		= "SOURCE";

	/**
	 * Tags describing the format. Like x264, XviD, DD5.1, AC3, 720p, 1080p, (subtitle tags), ... Not including the language tags.
	 */
	public static final String					CATEGORY_FORMAT		= "FORMAT";

	/**
	 * Language tags. Like German, GERMAN.CUSTOM.SUBBED, NLSUBBED, ...
	 */
	public static final String					CATEGORY_LANGUAGE	= "LANGUAGE";

	/**
	 * Tags for meta information about the release itself. Like PROPER, REPACK, READ INFO, iNTERNAL, DIRFIX, ...
	 */
	public static final String					CATEGORY_META		= "META";

	public static List<Tag> list(Collection<String> tags)
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

	public static List<Tag> list(String... tags)
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

	private final String	name;
	private final String	longName;

	public Tag(String name)
	{
		this(name, null);
	}

	public Tag(String name, String longName)
	{
		this.name = name;
		this.longName = longName;
	}

	public String getName()
	{
		return name;
	}

	public String getLongName()
	{
		return longName;
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
			return StringUtils.equalsIgnoreCase(name, ((Tag) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(21, 47).append(StringUtils.lowerCase(name)).toHashCode();
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Tag o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}
}
