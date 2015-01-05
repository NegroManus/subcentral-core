package de.subcentral.core.model.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media item that has no own class. For audio / video media items see {@link RegularAvMedia}.
 *
 */
public class RegularMedia extends AbstractNamedMedia
{
	public static final SimplePropDescriptor	PROP_NAME						= new SimplePropDescriptor(RegularMedia.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE						= new SimplePropDescriptor(RegularMedia.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE			= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE					= new SimplePropDescriptor(RegularMedia.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(RegularMedia.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES			= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES						= new SimplePropDescriptor(RegularMedia.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION				= new SimplePropDescriptor(RegularMedia.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_LINKS				= new SimplePropDescriptor(RegularMedia.class, PropNames.COVER_LINKS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING				= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFORMATION_LINKS	= new SimplePropDescriptor(RegularMedia.class,
																						PropNames.FURTHER_INFO_LINKS);

	protected String							mediaType;
	protected String							mediaContentType;
	protected final List<String>				originalLanguages				= new ArrayList<>(1);
	protected final List<String>				countriesOfOrigin				= new ArrayList<>(1);
	protected final Set<String>					genres							= new HashSet<>(3);

	public RegularMedia()
	{

	}

	public RegularMedia(String name)
	{
		this.name = name;
	}

	@Override
	public String getMediaType()
	{
		return mediaType;
	}

	public void setMediaType(String mediaType)
	{
		this.mediaType = mediaType;
	}

	@Override
	public String getMediaContentType()
	{
		return mediaContentType;
	}

	public void setMediaContentType(String mediaContentType)
	{
		this.mediaContentType = mediaContentType;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		this.genres.clear();
		this.genres.addAll(genres);
	}

	@Override
	public List<String> getOriginalLanguages()
	{
		return originalLanguages;
	}

	public void setOriginalLanguages(List<String> originalLanguages)
	{
		this.originalLanguages.clear();
		this.originalLanguages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(List<String> countriesOfOrigin)
	{
		this.countriesOfOrigin.clear();
		this.countriesOfOrigin.addAll(countriesOfOrigin);
	}

	// convenience
	@Override
	public String getPrimaryOriginalLanguage()
	{
		return !originalLanguages.isEmpty() ? originalLanguages.get(0) : null;
	}

	@Override
	public String getPrimaryCountryOfOrigin()
	{
		return !countriesOfOrigin.isEmpty() ? countriesOfOrigin.get(0) : null;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof RegularMedia)
		{
			return StringUtils.equalsIgnoreCase(name, ((RegularMedia) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(StringUtils.lowerCase(name)).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(RegularMedia.class)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("mediaType", mediaType)
				.add("mediaContentType", mediaContentType)
				.add("date", date)
				.add("originalLanguages", Models.nullIfEmpty(originalLanguages))
				.add("countriesOfOrigin", Models.nullIfEmpty(countriesOfOrigin))
				.add("genres", Models.nullIfEmpty(genres))
				.add("description", description)
				.add("coverLinks", Models.nullIfEmpty(coverLinks))
				.add("contentRating", contentRating)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoLinks", Models.nullIfEmpty(furtherInfoLinks))
				.add("attributes", Models.nullIfEmpty(attributes))
				.toString();
	}
}
