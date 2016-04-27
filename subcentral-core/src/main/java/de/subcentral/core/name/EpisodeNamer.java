package de.subcentral.core.name;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.util.Context;

public class EpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the episode's series is included in the name, otherwise it is excluded. The default value is
	 * {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SERIES		= EpisodeNamer.class.getName() + ".includeSeries";

	/**
	 * The name of the parameter "includeSeason" of type {@link Boolean}. If set to {@code true}, the episode's season is included in the name, otherwise it is excluded. The default value is
	 * {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SEASON		= EpisodeNamer.class.getName() + ".includeSeason";

	/**
	 * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the episode's title is always included in the name, otherwise only if the episode is not
	 * numbered. The default value is {@code false}.
	 */
	public static final String	PARAM_ALWAYS_INCLUDE_TITLE	= EpisodeNamer.class.getName() + ".alwaysIncludeTitle";

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
	protected void appendName(PropSequenceNameBuilder b, Episode epi, Context ctx)
	{
		// add series
		if (epi.getSeries() != null && ctx.getBoolean(PARAM_INCLUDE_SERIES, Boolean.TRUE))
		{
			seriesNamer.appendName(b, epi.getSeries(), ctx);
		}

		// add season
		if (epi.isPartOfSeason() && ctx.getBoolean(PARAM_INCLUDE_SEASON, Boolean.TRUE))
		{
			// season namer must not include series as it was already used
			// so just use buildOwnName
			seasonNamer.appendOwnName(b, epi.getSeason(), ctx);
		}

		appendOwnName(b, epi, ctx);
	}

	protected void appendOwnName(PropSequenceNameBuilder b, Episode epi, Context ctx)
	{
		boolean sufficientlyNamed = false;
		// add season
		if (epi.isPartOfSeason())
		{
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
		if (epi.isTitled() && (!sufficientlyNamed || ctx.getBoolean(PARAM_ALWAYS_INCLUDE_TITLE, Boolean.FALSE)))
		{
			b.append(Episode.PROP_TITLE, epi.getTitle());
		}
	}
}
