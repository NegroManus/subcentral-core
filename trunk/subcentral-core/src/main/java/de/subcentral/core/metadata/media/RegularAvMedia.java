package de.subcentral.core.metadata.media;

import com.google.common.base.MoreObjects;

import de.subcentral.core.BeanUtil;
import de.subcentral.core.PropNames;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * For any audio/video media item that has no own class. Like shows, documentations, concerts ...
 *
 */
public class RegularAvMedia extends RegularMedia implements AvMedia
{
	public static final SimplePropDescriptor	PROP_NAME					= new SimplePropDescriptor(RegularAvMedia.class, PropNames.NAME);
	public static final SimplePropDescriptor	PROP_TITLE					= new SimplePropDescriptor(RegularAvMedia.class, PropNames.TITLE);
	public static final SimplePropDescriptor	PROP_MEDIA_TYPE				= new SimplePropDescriptor(RegularAvMedia.class, PropNames.MEDIA_TYPE);
	public static final SimplePropDescriptor	PROP_MEDIA_CONTENT_TYPE		= new SimplePropDescriptor(RegularAvMedia.class,
																					PropNames.MEDIA_CONTENT_TYPE);
	public static final SimplePropDescriptor	PROP_DATE					= new SimplePropDescriptor(RegularAvMedia.class, PropNames.DATE);
	public static final SimplePropDescriptor	PROP_ORIGINAL_LANGUAGES		= new SimplePropDescriptor(RegularAvMedia.class,
																					PropNames.ORIGINAL_LANGUAGES);
	public static final SimplePropDescriptor	PROP_COUNTRIES_OF_ORIGIN	= new SimplePropDescriptor(RegularAvMedia.class,
																					PropNames.COUNTRIES_OF_ORIGIN);
	public static final SimplePropDescriptor	PROP_RUNNING_TIME			= new SimplePropDescriptor(RegularAvMedia.class, PropNames.RUNNING_TIME);
	public static final SimplePropDescriptor	PROP_GENRES					= new SimplePropDescriptor(RegularAvMedia.class, PropNames.GENRES);
	public static final SimplePropDescriptor	PROP_DESCRIPTION			= new SimplePropDescriptor(RegularAvMedia.class, PropNames.DESCRIPTION);
	public static final SimplePropDescriptor	PROP_COVER_LINKS			= new SimplePropDescriptor(RegularAvMedia.class, PropNames.COVER_LINKS);
	public static final SimplePropDescriptor	PROP_CONTENT_RATING			= new SimplePropDescriptor(RegularAvMedia.class, PropNames.CONTENT_RATING);
	public static final SimplePropDescriptor	PROP_CONTRIBUTIONS			= new SimplePropDescriptor(RegularAvMedia.class, PropNames.CONTRIBUTIONS);
	public static final SimplePropDescriptor	PROP_FURTHER_INFO_LINKS		= new SimplePropDescriptor(RegularAvMedia.class,
																					PropNames.FURTHER_INFO_LINKS);

	protected int								runningTime					= 0;

	public RegularAvMedia()
	{

	}

	public RegularAvMedia(String name)
	{
		this.name = name;
	}

	@Override
	public void setMediaContentType(String mediaContentType) throws IllegalArgumentException
	{
		super.setMediaContentType(BeanUtil.validateString(mediaContentType,
				"mediaContentType",
				Media.MEDIA_CONTENT_TYPE_AUDIO,
				Media.MEDIA_CONTENT_TYPE_VIDEO));
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
	public String toString()
	{
		return MoreObjects.toStringHelper(RegularAvMedia.class)
				.omitNullValues()
				.add("name", name)
				.add("title", title)
				.add("mediaType", mediaType)
				.add("mediaContentType", mediaContentType)
				.add("date", date)
				.add("originalLanguages", BeanUtil.nullIfEmpty(originalLanguages))
				.add("countriesOfOrigin", BeanUtil.nullIfEmpty(countriesOfOrigin))
				.add("runningTime", BeanUtil.nullIfZero(runningTime))
				.add("genres", BeanUtil.nullIfEmpty(genres))
				.add("description", description)
				.add("coverLinks", BeanUtil.nullIfEmpty(coverLinks))
				.add("contentRating", contentRating)
				.add("contributions", BeanUtil.nullIfEmpty(contributions))
				.add("furtherInfoLinks", BeanUtil.nullIfEmpty(furtherInfoLinks))
				.add("attributes", BeanUtil.nullIfEmpty(attributes))
				.toString();
	}
}
