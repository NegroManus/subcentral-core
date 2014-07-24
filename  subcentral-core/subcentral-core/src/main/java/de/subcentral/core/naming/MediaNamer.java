package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Media;

public class MediaNamer implements Namer<Media>
{
	@Override
	public Class<Media> getType()
	{
		return Media.class;
	}

	@Override
	public String name(Media media, NamingService namingService, Map<String, Object> parameters) throws NamingException
	{
		return media.getName();
	}

}
