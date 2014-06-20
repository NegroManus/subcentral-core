package de.subcentral.core.media;

import java.util.List;

public interface MediaCollection<M extends MediaItem>
{
	public List<M> getMediaItems();
}
