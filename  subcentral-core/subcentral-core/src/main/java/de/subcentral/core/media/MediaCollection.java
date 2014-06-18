package de.subcentral.core.media;

import java.util.List;

public interface MediaCollection<M extends Media>
{
	public List<M> getMedia();
}
