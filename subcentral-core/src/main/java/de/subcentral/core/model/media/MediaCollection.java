package de.subcentral.core.model.media;

import java.util.List;

public interface MediaCollection<M extends Media> extends Media
{
	public List<M> getMediaItems();
}
