package de.subcentral.core.metadata.media;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.Settings;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media type that has no own class, like a movie, or if the media type could not be determined.
 *
 */
public class GenericMedia extends SingleMedia implements Comparable<GenericMedia>
{
	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(GenericMedia.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(GenericMedia.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE	= new SimplePropDescriptor(GenericMedia.class, PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE			= new SimplePropDescriptor(GenericMedia.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(GenericMedia.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(GenericMedia.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES			= new SimplePropDescriptor(GenericMedia.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_GENRES				= new SimplePropDescriptor(GenericMedia.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME		= new SimplePropDescriptor(GenericMedia.class, PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(GenericMedia.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_IMAGES				= new SimplePropDescriptor(GenericMedia.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(GenericMedia.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS	= new SimplePropDescriptor(GenericMedia.class, PropNames.FURTHER_INFO_LINKS);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(GenericMedia.class, PropNames.ATTRIBUTES);

	private String	mediaType;
	private String	mediaContentType;

	public GenericMedia()
	{

	}

	public GenericMedia(String name)
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

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof GenericMedia)
		{
			return StringUtils.equalsIgnoreCase(name, ((GenericMedia) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 23).append(StringUtils.lowerCase(name)).toHashCode();
	}

	@Override
	public int compareTo(GenericMedia o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return Settings.STRING_ORDERING.compare(name, o.name);
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(GenericMedia.class)
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
				.add("furtherInfoLinks", BeanUtil.nullIfEmpty(furtherInfoLinks))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
