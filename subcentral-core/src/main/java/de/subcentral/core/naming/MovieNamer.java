package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.metadata.media.Movie;

public class MovieNamer extends AbstractPropertySequenceNamer<Movie>
{
	public MovieNamer(PropSequenceNameBuilder.Config config)
	{
		super(config);
	}

	/**
	 * The name of the parameter "includeYear" of type {@link Boolean}. If set to {@code true}, the {@link Movie#getYear() movie's year} is included in the name, otherwise it is excluded. The default
	 * value is {@code false}.
	 */
	public static final String PARAM_INCLUDE_YEAR = MovieNamer.class.getName() + "includeYear";

	@Override
	public void buildName(PropSequenceNameBuilder b, Movie mov, Map<String, Object> params)
	{
		// read naming parameters
		boolean includeYear = NamingUtil.readParameter(params, PARAM_INCLUDE_YEAR, Boolean.class, Boolean.FALSE);
		if (includeYear)
		{
			b.appendIfNotNull(Movie.PROP_NAME, mov.getTitleOrName());
			b.appendIfNotNull(Movie.PROP_DATE, mov.getYear());
		}
		else
		{
			b.appendIfNotNull(Movie.PROP_NAME, mov.getName());
		}
	}
}
