package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Separation;

public class MiniSeriesEpisodeNamer extends AbstractEpisodeNamer
{
	protected MiniSeriesEpisodeNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// settings
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES_KEY, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		boolean alwaysIncludeEpisodeTitle = Namings.readParameter(params,
				PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY,
				Boolean.class,
				PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_DEFAULT);

		// add series
		if (includeSeries && epi.getSeries() != null)
		{
			b.append(Episode.PROP_SERIES, epi.getSeries().getName());
		}

		// add episode
		b.appendIf(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries(), epi.isNumberedInSeries());
		b.appendIf(Episode.PROP_TITLE, epi.getTitle(), (alwaysIncludeEpisodeTitle || !epi.isNumberedInSeries()) && epi.isTitled());
	}
}
