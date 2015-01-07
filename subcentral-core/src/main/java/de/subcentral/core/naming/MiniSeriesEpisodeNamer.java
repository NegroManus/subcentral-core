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
		// add series
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		if (includeSeries && epi.getSeries() != null)
		{
			boolean useSeriesTitle = Namings.readParameter(params, PARAM_USE_SERIES_TITLE, Boolean.class, PARAM_USE_SERIES_TITLE_DEFAULT);
			b.appendIfNotNull(Episode.PROP_SERIES, useSeriesTitle ? epi.getSeries().getTitleOrName() : epi.getSeries().getName());
		}

		// add episode
		if (epi.isNumberedInSeries())
		{
			b.append(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries());
			boolean alwaysIncludeEpisodeTitle = Namings.readParameter(params,
					PARAM_ALWAYS_INCLUDE_TITLE,
					Boolean.class,
					PARAM_ALWAYS_INCLUDE_TITLE_DEFAULT);
			b.appendIf(Episode.PROP_TITLE, epi.getTitle(), alwaysIncludeEpisodeTitle && epi.isTitled());
		}
		else
		{
			b.appendIfNotNull(Episode.PROP_TITLE, epi.getTitle());
		}
	}
}
