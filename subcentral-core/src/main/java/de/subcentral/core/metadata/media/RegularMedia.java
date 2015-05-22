package de.subcentral.core.metadata.media;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media item that has no own class. For audio / video media items see {@link RegularMedia}.
 *
 */
public class RegularMedia extends AbstractNamedMedia
{
	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(RegularMedia.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(RegularMedia.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE	= new SimplePropDescriptor(RegularMedia.class, PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE			= new SimplePropDescriptor(RegularMedia.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(RegularMedia.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(RegularMedia.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES			= new SimplePropDescriptor(RegularMedia.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_GENRES				= new SimplePropDescriptor(RegularMedia.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME		= new SimplePropDescriptor(RegularMedia.class, PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(RegularMedia.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_IMAGES				= new SimplePropDescriptor(RegularMedia.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(RegularMedia.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO		= new SimplePropDescriptor(RegularMedia.class, PropNames.FURTHER_INFO);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(RegularMedia.class, PropNames.ATTRIBUTES);

	private String								mediaType;
	private String								mediaContentType;
	private final List<String>					languages				= new ArrayList<>(1);
	private final List<String>					countries				= new ArrayList<>(1);
	private final Set<String>					genres					= new HashSet<>(3);
	private int									runningTime				= 0;

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
	public List<String> getLanguages()
	{
		return languages;
	}

	public void setLanguages(List<String> originalLanguages)
	{
		this.languages.clear();
		this.languages.addAll(originalLanguages);
	}

	@Override
	public List<String> getCountries()
	{
		return countries;
	}

	public void setCountries(List<String> countriesOfOrigin)
	{
		this.countries.clear();
		this.countries.addAll(countriesOfOrigin);
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
	public int getRunningTime()
	{
		return runningTime;
	}

	public void setRunningTime(int runningTime)
	{
		this.runningTime = runningTime;
	}

	// convenience
	@Override
	public String getPrimaryOriginalLanguage()
	{
		return !languages.isEmpty() ? languages.get(0) : null;
	}

	@Override
	public String getPrimaryCountryOfOrigin()
	{
		return !countries.isEmpty() ? countries.get(0) : null;
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
				.add("aliasNames", BeanUtil.nullIfEmpty(aliasNames))
				.add("title", title)
				.add("mediaType", mediaType)
				.add("mediaContentType", mediaContentType)
				.add("date", date)
				.add("languages", BeanUtil.nullIfEmpty(languages))
				.add("countries", BeanUtil.nullIfEmpty(countries))
				.add("runningTime", BeanUtil.nullIfZero(runningTime))
				.add("genres", BeanUtil.nullIfEmpty(genres))
				.add("description", description)
				.add("ratings", BeanUtil.nullIfEmpty(ratings))
				.add("contentRating", contentRating)
				.add("images", BeanUtil.nullIfEmpty(images))
				.add("furtherInfo", BeanUtil.nullIfEmpty(furtherInfo))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
