package de.subcentral.core.naming;

import de.subcentral.core.subtitle.Subtitle;

public class SubtitleNamer implements Namer<Subtitle>
{
	@Override
	public Class<Subtitle> getType()
	{
		return Subtitle.class;
	}

	@Override
	public String name(Subtitle sub, NamingService namingService)
	{
		if (sub == null)
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(namingService.name(sub.getMediaItem()));
		sb.append(' ');
		sb.append(sub.getLanguage());
		return sb.toString();
	}
}
