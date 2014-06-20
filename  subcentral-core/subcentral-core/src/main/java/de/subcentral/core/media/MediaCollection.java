package de.subcentral.core.media;

import java.util.List;

public interface MediaCollection<M extends MediaItem> extends Media
{
	public List<M> getMediaItems();

	@Override
	public default String getMediaType()
	{
		return Media.TYPE_COLLECTION;
	}
}
