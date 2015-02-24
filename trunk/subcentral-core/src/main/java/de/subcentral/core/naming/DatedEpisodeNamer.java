package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Episode;

public class DatedEpisodeNamer extends AbstractEpisodeNamer
{
	public DatedEpisodeNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// add series
		boolean includeSeries = NamingUtils.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, PARAM_INCLUDE_SERIES_DEFAULT);
		if (includeSeries && epi.getSeries() != null)
		{
			boolean useSeriesTitle = NamingUtils.readParameter(params, PARAM_USE_SERIES_TITLE, Boolean.class, PARAM_USE_SERIES_TITLE_DEFAULT);
			b.appendIfNotNull(Episode.PROP_SERIES, useSeriesTitle ? epi.getSeries().getTitleOrName() : epi.getSeries().getName());
		}

		// add episode
		if (epi.getDate() != null)
		{
			b.append(Episode.PROP_DATE, epi.getDate());
			boolean alwaysIncludeEpisodeTitle = NamingUtils.readParameter(params,
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
