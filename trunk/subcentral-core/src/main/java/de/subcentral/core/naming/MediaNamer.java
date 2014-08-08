package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Media;

public class MediaNamer implements Namer<Media>
{
	@Override
	public Class<Media> getEntityType()
	{
		return Media.class;
	}

	@Override
	public String name(Media media, Map<String, Object> parameters) throws NamingException
	{
		if (media == null)
		{
			return "";
		}
		return media.getName();
	}

}
