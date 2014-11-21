package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Separation;

public class DatedEpisodeNamer extends AbstractEpisodeNamer
{
	protected DatedEpisodeNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		boolean alwaysIncludeEpisodeTitle = Namings.readParameter(params,
				PARAM_ALWAYS_INCLUDE_TITLE,
				Boolean.class,
				PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_DEFAULT);

		// add series
		if (includeSeries && epi.getSeries() != null)
		{
			b.appendIfNotNull(Episode.PROP_SERIES, epi.getSeries().getName());
		}

		// add episode
		if (epi.getDate() != null)
		{
			b.append(Episode.PROP_DATE, epi.getDate());
			b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle && epi.isTitled());
		}
		else
		{
			b.appendIfNotNull(Episode.PROP_TITLE, epi.getTitle());
		}
	}
}
