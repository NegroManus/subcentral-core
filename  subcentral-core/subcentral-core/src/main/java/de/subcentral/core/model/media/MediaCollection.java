package de.subcentral.core.model.media;

import java.util.List;

public interface MediaCollection<M extends MediaItem> extends Media
{
	public static final String	PROP_NAME_MEDIA_ITEMS	= "mediaItems";

	@Override
	public default String getMediaType()
	{
		return Media.TYPE_COLLECTION;
	}

	public List<M> getMediaItems();
}
