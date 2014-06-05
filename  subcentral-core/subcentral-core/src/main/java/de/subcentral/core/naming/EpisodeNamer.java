package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;

public interface EpisodeNamer extends Namer<Episode>
{
	public default String name(Episode epi, boolean includeSeries, boolean includeSeason) throws NamingException
	{
		return name(epi, includeSeries, includeSeason, null);
	}

	public String name(Episode epi, boolean includeSeries, boolean includeSeason, NamingService namingService) throws NamingException;
}
