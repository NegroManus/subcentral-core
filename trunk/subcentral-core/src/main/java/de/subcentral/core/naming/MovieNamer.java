package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Movie;

public class MovieNamer extends AbstractPropertySequenceNamer<Movie>
{
	/**
	 * The parameter key for the Boolean value "includeYear". The default value is {@code true}.
	 */
	public static final String	PARAM_INCLUDE_YEAR_KEY	= "includeYear";

	@Override
	public String doName(Movie movie, Map<String, Object> params)
	{
		// settings
		boolean includeYear = Namings.readParameter(params, PARAM_INCLUDE_YEAR_KEY, Boolean.class, Boolean.FALSE);

		Builder b = newBuilder();
		b.append(Movie.PROP_NAME, movie.getTitleOrName());
		if (includeYear)
		{
			b.appendIfNotNull(Movie.PROP_DATE, movie.getYear());
		}
		return b.build();
	}
}
