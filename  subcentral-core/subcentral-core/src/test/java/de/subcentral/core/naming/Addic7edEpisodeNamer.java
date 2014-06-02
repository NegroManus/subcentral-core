package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;

public class Addic7edEpisodeNamer implements Namer<Episode>
{
	@Override
	public Class<Episode> getType()
	{
		return Episode.class;
	}

	@Override
	public String name(Episode obj, NamingService namingStrategy)
	{
		return new StringBuilder(obj.getSeries().getName()).append(" - ")
				.append(String.format("%02d", obj.getSeason().getNumber()))
				.append('x')
				.append(String.format("%02d", obj.getNumberInSeason()))
				.append(" - ")
				.append(obj.getTitle())
				.toString();
	}
}
