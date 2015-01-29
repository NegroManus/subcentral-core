package de.subcentral.core.metadata.release;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.Settings;
import de.subcentral.core.util.IterableComparator;

public class Tag implements Comparable<Tag>
{
	public static final Comparator<Iterable<Tag>>	TAGS_COMPARATOR		= IterableComparator.create();

	/**
	 * Tags describing the source. Like HDTV, BluRay, BDRip, DVDRip, ...
	 */
	public static final String						CATEGORY_SOURCE		= "SOURCE";

	/**
	 * Tags describing the format. Like x264, XviD, DD5.1, AC3, 720p, 1080p, (subtitle tags), ... Not including the language tags.
	 */
	public static final String						CATEGORY_FORMAT		= "FORMAT";

	/**
	 * Language tags. Like German, GERMAN.CUSTOM.SUBBED, NLSUBBED, MULTi, ...
	 */
	public static final String						CATEGORY_LANGUAGE	= "LANGUAGE";

	/**
	 * Tags for meta information about the release itself, not about the content. Like PROPER, REPACK, READ INFO, iNTERNAL, DIRFIX, ...
	 */
	public static final String						CATEGORY_META		= "META";

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

	public static ImmutableList<Tag> immutableList(String... tags)
	{
		if (tags.length == 0)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<Tag> tagList = ImmutableList.builder();
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList.build();
	}

	public static ImmutableList<Tag> immutableCopy(List<Tag> tags)
	{
		return ImmutableList.copyOf(tags);
	}

	private final String	name;
	private final String	longName;

	public Tag(String name)
	{
		this(name, null);
	}

	public Tag(String name, String longName)
	{
		this.name = Validate.notBlank(name, "name cannot be blank");
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
		if (obj instanceof Tag)
		{
			return name.equalsIgnoreCase(((Tag) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(21, 47).append(name.toLowerCase(Locale.ENGLISH)).toHashCode();
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
