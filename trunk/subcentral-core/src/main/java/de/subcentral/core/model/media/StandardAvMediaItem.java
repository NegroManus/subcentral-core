package de.subcentral.core.model.media;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.base.MoreObjects;

import de.subcentral.core.model.Models;
import de.subcentral.core.model.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any audio/video media item that has no own class. Like shows, documentations, concerts ...
 *
 */
public class StandardAvMediaItem extends StandardMediaItem implements AvMediaItem
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(StandardAvMediaItem.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(StandardAvMediaItem.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE				= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE		= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(StandardAvMediaItem.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES		= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN	= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME			= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(StandardAvMediaItem.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_URLS				= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.COVER_URLS);
	public static final SimplePropDescriptor	PROP_CONTENT_ADVISORY		= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.CONTENT_ADVISORY);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_URLS		= new SimplePropDescriptor(StandardAvMediaItem.class,
																					PropNames.FURTHER_INFO_URLS);

	protected int								runningTime					= 0;

	public StandardAvMediaItem()
	{

	}

	public StandardAvMediaItem(String name)
	{
		this.name = name;
	}

	@Override
	public void setMediaContentType(String mediaContentType) throws IllegalArgumentException
	{
		Medias.validateMediaContentType(new String[] { Media.MEDIA_CONTENT_TYPE_AUDIO, Media.MEDIA_CONTENT_TYPE_VIDEO }, mediaContentType);
		super.setMediaContentType(mediaContentType);
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

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj != null && obj instanceof StandardAvMediaItem)
		{
			return Objects.equals(name, ((StandardAvMediaItem) obj).name);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(67, 65).append(name).toHashCode();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(StandardAvMediaItem.class)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("mediaType", mediaType)
				.add("mediaContentType", mediaContentType)
				.add("date", date)
				.add("originalLanguages", Models.nullIfEmpty(originalLanguages))
				.add("countriesOfOrigin", Models.nullIfEmpty(countriesOfOrigin))
				.add("runningTime", runningTime)
				.add("genres", Models.nullIfEmpty(genres))
				.add("description", description)
				.add("coverUrls", Models.nullIfEmpty(coverUrls))
				.add("contentAdvisory", contentAdvisory)
				.add("contributions", Models.nullIfEmpty(contributions))
				.add("furtherInfoUrls", Models.nullIfEmpty(furtherInfoUrls))
				.toString();
	}
}
