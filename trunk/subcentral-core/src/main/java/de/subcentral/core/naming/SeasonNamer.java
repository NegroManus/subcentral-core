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
	 * The parameter key for the Boolean value "includeSeries". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_SERIES	= SeasonNamer.class.getName() + ".includeSeries";

	protected SeasonNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}

	@Override
	public void buildName(PropSequenceNameBuilder b, Season season, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeSeries = Namings.readParameter(params, PARAM_INCLUDE_SERIES, Boolean.class, Boolean.TRUE);

		// add series
		if (includeSeries && season.getSeries() != null)
		{
			b.appendIfNotNull(Episode.PROP_SERIES, season.getSeries().getName());
		}
		b.appendIfNotNull(Season.PROP_NUMBER, season.getNumber());
		b.appendIfNotNull(Season.PROP_TITLE, season.getTitle());
	}
}
