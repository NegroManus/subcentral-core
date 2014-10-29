package de.subcentral.core.naming;

import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Separation;

public abstract class AbstractEpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The parameter key for the Boolean value "includeSeries".
	 */
	public static final String	PARAM_INCLUDE_SERIES_KEY					= "includeSeries";
	public static final Boolean	PARAM_INCLUDE_SERIES_DEFAULT				= Boolean.TRUE;

	/**
	 * The parameter key for the Boolean value "alwaysIncludeEpisodeTitle".
	 */
	public static final String	PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_KEY		= "alwaysIncludeEpisodeTitle";
	public static final Boolean	PARAM_ALWAYS_INCLUDE_EPISODE_TITLE_DEFAULT	= Boolean.FALSE;

	protected AbstractEpisodeNamer(PropToStringService propToStringService, Set<Separation> separators, Function<String, String> finalFormatter)
	{
		super(propToStringService, separators, finalFormatter);
	}
}
