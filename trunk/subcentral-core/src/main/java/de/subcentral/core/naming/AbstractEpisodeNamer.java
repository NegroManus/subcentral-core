package de.subcentral.core.naming;

import java.util.Set;
import java.util.function.Function;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.util.Separation;

public abstract class AbstractEpisodeNamer extends AbstractPropertySequenceNamer<Episode>
{
	/**
	 * The name of the parameter "includeSeries" of type {@link Boolean}. If set to {@code true}, the episode's series is included in the name,
	 * otherwise it is excluded. The default value is {@code true}.
	 */
	public static final String		PARAM_INCLUDE_SERIES				= AbstractEpisodeNamer.class.getName() + ".includeSeries";
	protected static final Boolean	PARAM_INCLUDE_SERIES_DEFAULT		= Boolean.TRUE;

	/**
	 * The name of the parameter "alwaysIncludeTitle" of type {@link Boolean}. If set to {@code true}, the episode's title is always included in the
	 * name, otherwise only if the episode is not numbered. The default value is {@code false}.
	 */
	public static final String		PARAM_ALWAYS_INCLUDE_TITLE			= AbstractEpisodeNamer.class.getName() + ".alwaysIncludeTitle";
	protected static final Boolean	PARAM_ALWAYS_INCLUDE_TITLE_DEFAULT	= Boolean.FALSE;

	protected AbstractEpisodeNamer(PropToStringService propToStringService, String defaultSeparator, Set<Separation> separations,
			Function<String, String> finalFormatter)
	{
		super(propToStringService, defaultSeparator, separations, finalFormatter);
	}
}
