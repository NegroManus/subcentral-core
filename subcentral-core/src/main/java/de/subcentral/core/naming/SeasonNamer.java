package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season>
{
    /**
     * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the season's series is included in the name,
     * otherwise it is excluded. The default value is {@code true}.
     */
    public static final String PARAM_INCLUDE_SERIES = SeasonNamer.class.getName() + ".includeSeries";

    /**
     * The name of the parameter "seriesName" of type {@link String}. The specified name is used for naming the season. The default value is the
     * return value of {@link Series#getName()}. But for example any alias name may be used.
     */
    public static final String PARAM_SERIES_NAME = EpisodeNamer.class.getName() + ".seriesName";

    /**
     * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the title of the season is always included in
     * the name, otherwise only if the season is not numbered. The default value is {@code false}.
     */
    public static final String PARAM_ALWAYS_INCLUDE_TITLE = SeasonNamer.class.getName() + ".alwaysIncludeTitle";

    public SeasonNamer(PropSequenceNameBuilder.Config config)
    {
	super(config);
    }

    @Override
    public void buildName(PropSequenceNameBuilder b, Season season, Map<String, Object> params)
    {
	// read naming parameters
	boolean includeSeries = NamingUtil.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, Boolean.TRUE);
	boolean alwaysIncludeTitle = NamingUtil.readParameter(params, PARAM_ALWAYS_INCLUDE_TITLE, Boolean.class, Boolean.FALSE);

	// add series
	if (includeSeries && season.getSeries() != null)
	{
	    String name = NamingUtil.readParameter(params, PARAM_SERIES_NAME, String.class, season.getSeries().getName());
	    b.appendIfNotNull(Series.PROP_NAME, name);
	}
	if (season.isNumbered())
	{
	    b.append(Season.PROP_NUMBER, season.getNumber());
	    b.appendIf(Season.PROP_TITLE, season.getTitle(), alwaysIncludeTitle && season.isTitled());
	}
	else
	{
	    b.appendIfNotNull(Season.PROP_TITLE, season.getTitle());
	}
    }
}
