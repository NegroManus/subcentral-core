package de.subcentral.core.model.media;

import java.util.List;

public interface MediaCollection<M extends MediaItem> extends Media
{
	@Override
	public default String getMediaContentType()
	{
		return Media.CONTENT_TYPE_COLLECTION;
	}

	public List<M> getMediaItems();
}
