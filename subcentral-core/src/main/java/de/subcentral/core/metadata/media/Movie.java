package de.subcentral.core.metadata.media;

import java.time.Year;
import java.util.Objects;

import com.google.common.base.MoreObjects;

import de.subcentral.core.PropNames;
import de.subcentral.core.util.ObjectUtil;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For Movies.
 *
 */
public class Movie extends StandaloneMedia implements Comparable<Movie>
{
	private static final long					serialVersionUID		= -8383150114871587705L;

	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(Movie.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_ALIAS_NAMES		= new SimplePropDescriptor(Movie.class, PropNames.ALIAS_NAMES);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(Movie.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE	= new SimplePropDescriptor(Movie.class, PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE			= new SimplePropDescriptor(Movie.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(Movie.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(Movie.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES			= new SimplePropDescriptor(Movie.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_GENRES				= new SimplePropDescriptor(Movie.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME		= new SimplePropDescriptor(Movie.class, PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(Movie.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_IMAGES				= new SimplePropDescriptor(Movie.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(Movie.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(Movie.class, PropNames.FURTHER_INFO_LINKS);
	public static final SimplePropDescriptor	PROP_IDS				= new SimplePropDescriptor(Movie.class, PropNames.IDS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(Movie.class, PropNames.ATTRIBUTES);

	public Movie()
	{
		// default constructor
	}

	public Movie(String name)
	{
		this.name = name;
	}

	public Movie(String name, Year year)
	{
		this.name = name;
		this.date = year;
	}

	@Override
	public String getMediaType()
	{
		return Media.MEDIA_TYPE_MOVIE;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.MEDIA_CONTENT_TYPE_VIDEO;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof Movie)
		{
			return Objects.equals(name, ((Movie) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(name);
	}

	@Override
	public int compareTo(Movie o)
	{
		if (o == this)
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

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(Movie.class)
				.omitNullValues()
				.add("name", name)
				.add("aliasNames", ObjectUtil.nullIfEmpty(aliasNames))
				.add("title", title)
				.add("date", date)
				.add("languages", ObjectUtil.nullIfEmpty(languages))
				.add("countries", ObjectUtil.nullIfEmpty(countries))
				.add("runningTime", ObjectUtil.nullIfZero(runningTime))
				.add("genres", ObjectUtil.nullIfEmpty(genres))
				.add("description", description)
				.add("ratings", ObjectUtil.nullIfEmpty(ratings))
				.add("contentRating", contentRating)
				.add("images", ObjectUtil.nullIfEmpty(images))
				.add("furtherInfoLinks", ObjectUtil.nullIfEmpty(furtherInfoLinks))
				.add("ids", ObjectUtil.nullIfEmpty(ids))
				.add("attributes", ObjectUtil.nullIfEmpty(attributes))
				.toString();
	}
}
