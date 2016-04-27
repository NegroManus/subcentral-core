package de.subcentral.core.metadata.release;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.IterableComparator;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.StringUtil;
import de.subcentral.core.util.ValidationUtil;

public class Tag implements Comparable<Tag>, Serializable
{
	private static final long						serialVersionUID	= -6045437773807621255L;

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

	private final String							name;
	private final String							longName;

	public Tag(String name)
	{
		this(name, null);
	}

	public Tag(String name, String longName) throws IllegalArgumentException
	{
		this.name = ValidationUtil.requireNotBlankAndStrip(name, "name cannot be blank");
		this.longName = longName;
	}

	public static Tag from(String name)
	{
		try
		{
			return new Tag(name);
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	public static String toStringNullSafe(Tag tag)
	{
		return tag != null ? tag.toString() : "";
	}

	public static List<Tag> list(Collection<String> tags)
	{
		List<Tag> tagList = new ArrayList<>(tags.size());
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}

	public static List<Tag> list(String... tags)
	{
		List<Tag> tagList = new ArrayList<>(tags.length);
		for (String s : tags)
		{
			tagList.add(new Tag(s));
		}
		return tagList;
	}

	public static ImmutableList<Tag> immutableList(String... tags)
	{
		ImmutableList.Builder<Tag> tagList = ImmutableList.builder();
		for (String tag : tags)
		{
			tagList.add(new Tag(tag));
		}
		return tagList.build();
	}

	public static List<Tag> parseList(String tagList)
	{
		return parseList(tagList, StringUtil.COMMA_SPLITTER);
	}

	public static List<Tag> parseList(String tagList, Splitter splitter)
	{
		return list(splitter.splitToList(tagList));
	}

	public static String formatList(List<Tag> tags)
	{
		return formatList(tags, StringUtil.COMMA_JOINER);
	}

	public static String formatList(List<Tag> tags, Joiner joiner)
	{
		return joiner.join(tags);
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
			return ObjectUtil.stringEqualIgnoreCase(name, ((Tag) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(Tag.class, ObjectUtil.stringHashCodeIgnoreCase(name));
	}

	@Override
	public String toString()
	{
		return name;
	}

	@Override
	public int compareTo(Tag o)
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
