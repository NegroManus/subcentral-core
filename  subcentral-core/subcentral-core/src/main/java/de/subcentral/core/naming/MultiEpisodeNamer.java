package de.subcentral.core.naming;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.MultiEpisode;

public class MultiEpisodeNamer extends AbstractNamer<MultiEpisode>
{
	@Override
	public Class<MultiEpisode> getType()
	{
		return MultiEpisode.class;
	}

	@Override
	protected String doName(MultiEpisode candidate, NamingService namingService) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append(candidate.getCommonSeries().getName());
		sb.append(' ');
		sb.append(candidate.getCommonSeason().getName());
		for (Episode epi : candidate)
		{
			sb.append('E');
			sb.append(epi.getNumberInSeason());
		}
		return sb.toString();
	}
}
