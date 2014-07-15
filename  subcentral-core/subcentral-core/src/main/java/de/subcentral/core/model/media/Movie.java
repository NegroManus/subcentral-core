package de.subcentral.core.model.media;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.util.Settings;
import de.subcentral.core.util.SimplePropertyDescriptor;

public class Movie extends AbstractAvMediaItem implements Comparable<Movie>
{
	public static final SimplePropertyDescriptor	PROP_NAME						= new SimplePropertyDescriptor(Movie.class, PROP_NAME_NAME);
	public static final SimplePropertyDescriptor	PROP_TITLE						= new SimplePropertyDescriptor(Movie.class, PROP_NAME_TITLE);
	public static final SimplePropertyDescriptor	PROP_MEDIA_TYPE					= new SimplePropertyDescriptor(Movie.class, PROP_NAME_MEDIA_TYPE);
	public static final SimplePropertyDescriptor	PROP_DATE						= new SimplePropertyDescriptor(Movie.class, PROP_NAME_DATE);
	public static final SimplePropertyDescriptor	PROP_ORIGINAL_LANGUAGE			= new SimplePropertyDescriptor(Movie.class,
																							PROP_NAME_ORIGINAL_LANGUAGE);
	public static final SimplePropertyDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropertyDescriptor(Movie.class,
																							PROP_NAME_COUNTRIES_OF_ORIGIN);
	public static final SimplePropertyDescriptor	PROP_GENRES						= new SimplePropertyDescriptor(Movie.class, PROP_NAME_GENRES);
	public static final SimplePropertyDescriptor	PROP_DESCRIPTION				= new SimplePropertyDescriptor(Movie.class, PROP_NAME_DESCRIPTION);
	public static final SimplePropertyDescriptor	PROP_COVER_URL					= new SimplePropertyDescriptor(Movie.class, PROP_NAME_COVER_URL);
	public static final SimplePropertyDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropertyDescriptor(Movie.class,
																							PROP_NAME_CONTENT_ADVISORY);
	public static final SimplePropertyDescriptor	PROP_FURHTER_INFORMATION_URLS	= new SimplePropertyDescriptor(Movie.class,
																							PROP_NAME_FURHTER_INFORMATION_URLS);

	private String									name;
	private Set<String>								genres							= new HashSet<>(4);
	private String									originalLanguage;
	private Set<String>								countriesOfOrigin				= new HashSet<>(1);

	public Movie()
	{

	}

	public Movie(String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public String getMediaType()
	{
		return Media.TYPE_VIDEO;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres = genres;
	}

	@Override
	public String getOriginalLanguage()
	{
		return originalLanguage;
	}

	public void setOriginalLanguage(String originalLanguage)
	{
		this.originalLanguage = originalLanguage;
	}

	@Override
	public Set<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(Set<String> countriesOfOrigin)
	{
		Validate.notNull(countriesOfOrigin, "countriesOfOrigin cannot be null");
		this.countriesOfOrigin = countriesOfOrigin;
	}

	// Object methods
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
		if (Movie.class != obj.getClass())
		{
			return false;
		}
		Movie o = (Movie) obj;
		return Objects.equal(name, o.name);
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(name).toHashCode();
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
				.add("originalLanguage", originalLanguage)
				.add("countriesOfOrigin", countriesOfOrigin)
				.add("runningTime", runningTime)
				.add("genres", genres)
				.add("description", description)
				.add("coverUrl", coverUrl)
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", contributions)
				.add("furtherInformationLinks", furtherInformationUrls)
				.toString();
	}
}
