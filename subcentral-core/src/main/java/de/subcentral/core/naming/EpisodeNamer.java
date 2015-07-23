package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;

public class EpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
    /**
     * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the episode's series is included in the name,
     * otherwise it is excluded. The default value is {@code true}.
     */
    public static final String PARAM_INCLUDE_SERIES = EpisodeNamer.class.getName() + ".includeSeries";

    /**
     * The name of the parameter "seriesName" of type {@link String}. The specified name is used for naming the episode. The default value is the
     * return value of {@link Series#getName()}. But for example any alias name may be used.
     */
    public static final String PARAM_SERIES_NAME = EpisodeNamer.class.getName() + ".seriesName";

    /**
     * The name of the parameter "includeSeason" of type {@link Boolean}. If set to {@code true}, the episode's season is included in the name,
     * otherwise it is excluded. The default value is {@code true}.
     */
    public static final String PARAM_INCLUDE_SEASON = EpisodeNamer.class.getName() + ".includeSeason";

    /**
     * The name of the parameter "alwaysIncludeSeasonTitle" of type {@link Boolean}. If set to {@code true}, the title of the episode's season is
     * always included in the name, otherwise only if the season is not numbered. The default value is {@code false}.
     */
    public static final String PARAM_ALWAYS_INCLUDE_SEASON_TITLE = EpisodeNamer.class.getName() + ".alwaysIncludeSeasonTitle";

    /**
     * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the episode's title is always included in the
     * name, otherwise only if the episode is not numbered. The default value is {@code false}.
     */
    public static final String PARAM_ALWAYS_INCLUDE_TITLE = EpisodeNamer.class.getName() + ".alwaysIncludeTitle";

    public EpisodeNamer(PropSequenceNameBuilder.Config config)
    {
	super(config);
    }

    @Override
    public void buildName(PropSequenceNameBuilder b, Episode epi, Map<String, Object> params)
    {
	// add series
	boolean includeSeries = NamingUtil.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, Boolean.TRUE);
	if (includeSeries && epi.getSeries() != null)
	{
	    String name = NamingUtil.readParameter(params, PARAM_SERIES_NAME, String.class, epi.getSeries().getName());
	    b.appendIfNotNull(Series.PROP_NAME, name);
	}

	// add episode number / date
	boolean sufficientlyNamed = false;
	if (epi.isPartOfSeason())
	{
	    boolean includeSeason = NamingUtil.readParameter(params, PARAM_INCLUDE_SEASON, Boolean.class, Boolean.TRUE);
	    if (includeSeason)
	    {
		Season season = epi.getSeason();
		if (season.isNumbered())
		{
		    b.append(Season.PROP_NUMBER, season.getNumber());
		    boolean alwaysIncludeSeasonTitle = NamingUtil.readParameter(params, PARAM_ALWAYS_INCLUDE_SEASON_TITLE, Boolean.class, Boolean.FALSE);
		    if (season.isTitled() && alwaysIncludeSeasonTitle)
		    {
			b.append(Season.PROP_TITLE, season.getTitle());
		    }
		}
		else
		{
		    b.appendIfNotNull(Season.PROP_TITLE, season.getTitle());
		}
	    }
	    if (epi.isNumberedInSeason())
	    {
		b.append(Episode.PROP_NUMBER_IN_SEASON, epi.getNumberInSeason());
		sufficientlyNamed = true;
	    }
	}
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
