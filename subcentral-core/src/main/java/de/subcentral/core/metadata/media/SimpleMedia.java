package de.subcentral.core.metadata.media;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any media type that has no own class, like a movie, or if the media type could not be determined.
 *
 */
public class SimpleMedia extends SingleMedia
{
	public static final SimplePropDescriptor	PROP_NAME				= new SimplePropDescriptor(SimpleMedia.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE				= new SimplePropDescriptor(SimpleMedia.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE	= new SimplePropDescriptor(SimpleMedia.class, PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE			= new SimplePropDescriptor(SimpleMedia.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_DATE				= new SimplePropDescriptor(SimpleMedia.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_LANGUAGES			= new SimplePropDescriptor(SimpleMedia.class, PropNames.LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES			= new SimplePropDescriptor(SimpleMedia.class, PropNames.COUNTRIES);
	public static final SimplePropDescriptor	PROP_GENRES				= new SimplePropDescriptor(SimpleMedia.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME		= new SimplePropDescriptor(SimpleMedia.class, PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_DESCRIPTION		= new SimplePropDescriptor(SimpleMedia.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_IMAGES				= new SimplePropDescriptor(SimpleMedia.class, PropNames.IMAGES);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING		= new SimplePropDescriptor(SimpleMedia.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO		= new SimplePropDescriptor(SimpleMedia.class, PropNames.FURTHER_INFO);
	public static final SimplePropDescriptor	PROP_ATTRIBUTES			= new SimplePropDescriptor(SimpleMedia.class, PropNames.ATTRIBUTES);

	private String	mediaType;
	private String	mediaContentType;

	public SimpleMedia()
	{

	}

	public SimpleMedia(String name)
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
		if (obj instanceof SimpleMedia)
		{
			return StringUtils.equalsIgnoreCase(name, ((SimpleMedia) obj).name);
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
		return MoreObjects.toStringHelper(SimpleMedia.class)
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
