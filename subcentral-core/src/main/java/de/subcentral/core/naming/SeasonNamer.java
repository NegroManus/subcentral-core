package de.subcentral.core.naming;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.util.Separation;

public class SeasonNamer extends AbstractPropertySequenceNamer<Season>
{
	/**
	 * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the season's series is included in the name,
	 * otherwise it is excluded. The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SERIES		= SeasonNamer.class.getName() + ".includeSeries";

	/**
	 * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the title of the season is always included in
	 * the name, otherwise only if the season is not numbered. The default value is {@code false}.
	 */
	public static final String	PARAM_ALWAYS_INCLUDE_TITLE	= SeasonNamer.class.getName() + ".alwaysIncludeTitle";

	protected SeasonNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Season season, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeSeries = NamingUtils.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, Boolean.TRUE);
		boolean alwaysIncludeTitle = NamingUtils.readParameter(params, PARAM_ALWAYS_INCLUDE_TITLE, Boolean.class, Boolean.FALSE);

		// add series
		if (includeSeries && season.getSeries() != null)
		{
			b.appendIfNotNull(Episode.PROP_SERIES, season.getSeries().getName());
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
