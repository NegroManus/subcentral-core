package de.subcentral.core.model.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.Objects;

import de.subcentral.core.Settings;
import de.subcentral.core.model.Prop;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For shows, documentations.
 *
 */
public class GenericAvMediaItem extends AbstractAvMediaItem implements Comparable<GenericAvMediaItem>
{
	public static final SimplePropDescriptor	PROP_NAME						= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.NAME);
	public static final SimplePropDescriptor	PROP_TITLE						= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE					= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE						= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES			= new SimplePropDescriptor(GenericAvMediaItem.class,
																						Prop.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN		= new SimplePropDescriptor(GenericAvMediaItem.class,
																						Prop.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_GENRES						= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION				= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS					= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY			= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS				= new SimplePropDescriptor(GenericAvMediaItem.class, Prop.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFORMATION_URLS	= new SimplePropDescriptor(GenericAvMediaItem.class,
																						Prop.FURTHER_INFO_URLS);

	private String								name;
	private String								mediaContentType;
	private String								mediaType;
	private List<String>						originalLanguages				= new ArrayList<>(1);
	private List<String>						countriesOfOrigin				= new ArrayList<>(1);
	private Set<String>							genres							= new HashSet<>(3);

	public GenericAvMediaItem()
	{

	}

	public GenericAvMediaItem(String name)
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
	public String getMediaType()
	{
		return mediaType;
	}

	public void setMediaType(String mediaType)
	{
		this.mediaType = mediaType;
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
	public List<String> getOriginalLanguages()
	{
		return originalLanguages;
	}

	public void setOriginalLanguages(List<String> originalLanguages)
	{
		this.originalLanguages = originalLanguages;
	}

	@Override
	public List<String> getCountriesOfOrigin()
	{
		return countriesOfOrigin;
	}

	public void setCountriesOfOrigin(List<String> countriesOfOrigin)
	{
		Validate.notNull(countriesOfOrigin, "countriesOfOrigin cannot be null");
		this.countriesOfOrigin = countriesOfOrigin;
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
		if (obj != null && GenericAvMediaItem.class.equals(obj.getClass()))
		{
			GenericAvMediaItem o = (GenericAvMediaItem) obj;
			return Objects.equal(name, o.name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(name).toHashCode();
	}

	@Override
	public int compareTo(GenericAvMediaItem o)
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
