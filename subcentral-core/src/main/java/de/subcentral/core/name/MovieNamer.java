package de.subcentral.core.name;

import java.util.Map;

import de.subcentral.core.metadata.media.Movie;

public class MovieNamer extends AbstractNamedMediaNamer<Movie>
{
	/**
	 * The name of the parameter "includeYear" of type {@link Boolean}. If set to {@code true}, the {@link Movie#getYear() movie's year} is included in the name, otherwise it is excluded. The default
	 * value is {@code false}.<br/>
	 * If no explicit name is set via the {@link AbstractNamedMediaNamer#PARAM_NAME name parameter} the movie's title is used rather than the name (because the name can already include the year).
	 */
	public static final String PARAM_INCLUDE_YEAR = MovieNamer.class.getName() + "includeYear";

	public MovieNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	@Override
	protected void appendName(PropSequenceNameBuilder b, Movie mov, Map<String, Object> params)
	{
		boolean includeYear = NamingUtil.readParameter(params, PARAM_INCLUDE_YEAR, Boolean.class, Boolean.FALSE);
		if (includeYear)
		{
			String name = NamingUtil.readParameter(params, PARAM_NAME, String.class, mov.getTitleOrName());
			b.appendIfNotNull(Movie.PROP_NAME, name);
			b.appendIfNotNull(Movie.PROP_DATE, mov.getYear());
		}
		else
		{
			String name = NamingUtil.readParameter(params, PARAM_NAME, String.class, mov.getName());
			b.appendIfNotNull(Movie.PROP_NAME, name);
		}
	}
}
