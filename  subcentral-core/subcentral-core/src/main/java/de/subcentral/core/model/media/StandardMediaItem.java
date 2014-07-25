package de.subcentral.core.model.media;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.model.Models;
import de.subcentral.core.model.Prop;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media item that has no own class. For audio / video media items see {@link StandardAvMediaItem}.
 *
 */
public class StandardMediaItem extends AbstractMedia implements MediaItem
{
	public static final SimplePropDescriptor	PROP_NAME						= new SimplePropDescriptor(StandardMediaItem.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_TITLE						= new SimplePropDescriptor(StandardMediaItem.class, Prop.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE			= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE					= new SimplePropDescriptor(StandardMediaItem.class, Prop.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(StandardMediaItem.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES			= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES						= new SimplePropDescriptor(StandardMediaItem.class, Prop.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION				= new SimplePropDescriptor(StandardMediaItem.class, Prop.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS					= new SimplePropDescriptor(StandardMediaItem.class, Prop.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFORMATION_URLS	= new SimplePropDescriptor(StandardMediaItem.class,
																						Prop.FURTHER_INFO_URLS);

	protected String							name;
	protected String							mediaType;
	protected String							mediaContentType;
	protected Temporal							date;
	protected final List<String>				originalLanguages				= new ArrayList<>(1);
	protected final List<String>				countriesOfOrigin				= new ArrayList<>(1);
	protected final Set<String>					genres							= new HashSet<>(3);

	public StandardMediaItem()
	{

	}

	public StandardMediaItem(String name)
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
		Validate.validState(mediaContentType == null || Media.CONTENT_TYPE_AUDIO.equals(mediaContentType)
				|| Media.CONTENT_TYPE_VIDEO.equals(mediaContentType),
				"mediaContentType must be " + Media.CONTENT_TYPE_AUDIO + " or " + Media.CONTENT_TYPE_VIDEO);
		this.mediaContentType = mediaContentType;
	}

	@Override
	public Temporal getDate()
	{
		return date;
	}

	public void setDate(Temporal date)
	{
		this.date = date;
	}

	@Override
	public Set<String> getGenres()
	{
		return genres;
	}

	public void setGenres(Set<String> genres)
	{
		Validate.notNull(genres, "genres cannot be null");
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
		Validate.notNull(originalLanguages, "originalLanguages cannot be null");
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
		Validate.notNull(countriesOfOrigin, "countriesOfOrigin cannot be null");
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
		if (obj != null && obj instanceof StandardMediaItem)
		{
			return Objects.equal(name, ((StandardMediaItem) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(name).toHashCode();
	}

	@Override
	public String toString()
	{
		return Objects.toStringHelper(StandardMediaItem.class)
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
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
				.toString();
	}

}
