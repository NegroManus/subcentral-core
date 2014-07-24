package de.subcentral.core.model.media;

import java.time.Year;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Prop;
import de.subcentral.core.util.SimplePropDescriptor;

public class Movie extends StandardAvMediaItem implements Comparable<Movie>
{
	public static final SimplePropDescriptor	PROP_NAME						= new SimplePropDescriptor(Movie.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_TITLE						= new SimplePropDescriptor(Movie.class, Prop.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE					= new SimplePropDescriptor(Movie.class, Prop.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(Movie.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES			= new SimplePropDescriptor(Movie.class, Prop.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropDescriptor(Movie.class, Prop.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES						= new SimplePropDescriptor(Movie.class, Prop.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION				= new SimplePropDescriptor(Movie.class, Prop.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS					= new SimplePropDescriptor(Movie.class, Prop.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropDescriptor(Movie.class, Prop.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(Movie.class, Prop.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFORMATION_URLS	= new SimplePropDescriptor(Movie.class, Prop.FURTHER_INFO_URLS);

	public Movie()
	{

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
		return Media.TYPE_MOVIE;
	}

	@Override
	public String getMediaContentType()
	{
		return Media.CONTENT_TYPE_VIDEO;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && Movie.class.equals(obj.getClass()))
		{
			Movie o = (Movie) obj;
			return Objects.equal(name, o.name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(43, 91).append(name).toHashCode();
	}

	@Override
	public int compareTo(Movie o)
	{
		if (o == null)
		{
			return -1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(this)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("date", date)
				.add("originalLanguages", originalLanguages)
				.add("countriesOfOrigin", countriesOfOrigin)
				.add("runningTime", runningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrls", coverUrls)
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInfoUrls", furtherInfoUrls)
				.toString();
	}
}
