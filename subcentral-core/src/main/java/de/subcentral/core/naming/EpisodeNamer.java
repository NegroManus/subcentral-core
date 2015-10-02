package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Episode;

public class EpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the episode's series is included in the name, otherwise it is excluded. The default value is
	 * {@code true}.
	 */
	public static final String PARAM_INCLUDE_SERIES = EpisodeNamer.class.getName() + ".includeSeries";

	/**
	 * The name of the parameter "includeSeason" of type {@link Boolean}. If set to {@code true}, the episode's season is included in the name, otherwise it is excluded. The default value is
	 * {@code true}.
	 */
	public static final String PARAM_INCLUDE_SEASON = EpisodeNamer.class.getName() + ".includeSeason";

	/**
	 * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the episode's title is always included in the name, otherwise only if the episode is not
	 * numbered. The default value is {@code false}.
	 */
	public static final String PARAM_ALWAYS_INCLUDE_TITLE = EpisodeNamer.class.getName() + ".alwaysIncludeTitle";

	private final SeriesNamer	seriesNamer;
	private final SeasonNamer	seasonNamer;

	public EpisodeNamer(PropSequenceNameBuilder.Config config)
	{
		this(config, null, null);
	}

	public EpisodeNamer(PropSequenceNameBuilder.Config config, SeriesNamer seriesNamer, SeasonNamer seasonNamer)
	{
		super(config);
		this.seriesNamer = seriesNamer != null ? seriesNamer : new SeriesNamer(config);
		this.seasonNamer = seasonNamer != null ? seasonNamer : new SeasonNamer(config);
	}

	public SeriesNamer getSeriesNamer()
	{
		return seriesNamer;
	}

	public SeasonNamer getSeasonNamer()
	{
		return seasonNamer;
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
	{
		// add series
		boolean includeSeries = NamingUtil.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, Boolean.TRUE);
		if (includeSeries && epi.getSeries() != null)
		{
			seriesNamer.buildName(b, epi.getSeries(), params);
		}

		boolean sufficientlyNamed = false;
		// add season
		if (epi.isPartOfSeason())
		{
			boolean includeSeason = NamingUtil.readParameter(params, PARAM_INCLUDE_SEASON, Boolean.class, Boolean.TRUE);
			if (includeSeason)
			{
				// season namer must not include series as it was already used
				// so just use buildOwnName
				seasonNamer.buildOwnName(b, epi.getSeason(), params);
			}
			if (epi.isNumberedInSeason())
			{
				b.append(Episode.PROP_NUMBER_IN_SEASON, epi.getNumberInSeason());
				sufficientlyNamed = true;
			}
		}
		// add episode number / date
		else if (epi.isNumberedInSeries())
		{
			b.append(Episode.PROP_NUMBER_IN_SERIES, epi.getNumberInSeries());
			sufficientlyNamed = true;
		}
		else if (epi.getDate() != null)
		{
			b.append(Episode.PROP_DATE, epi.getDate());
			sufficientlyNamed = true;
		}

		// may add episode title
		boolean alwaysIncludeTitle = NamingUtil.readParameter(params, PARAM_ALWAYS_INCLUDE_TITLE, Boolean.class, Boolean.FALSE);
		if (epi.isTitled() && (!sufficientlyNamed || alwaysIncludeTitle))
		{
			b.append(Episode.PROP_TITLE, epi.getTitle());
		}
	}
}
