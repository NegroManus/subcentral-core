package de.subcentral.core.naming;

import de.subcentral.core.media.Media;

public class MediaNamer implements Namer<Media>
{
	@Override
	public Class<Media> getType()
	{
		return Media.class;
	}

	@Override
	public String name(Media obj, NamingService namingStrategy)
	{
		return obj.getTitle();
	}

}
