package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Movie;

public class MovieNamer extends AbstractPropertySequenceNamer<Movie>
{
	/**
	 * The parameter key for the Boolean value "includeYear".
	 */
	public static final String	PARAM_INCLUDE_YEAR_KEY		= "includeYear";
	public static final Boolean	PARAM_INCLUDE_YEAR_DEFAULT	= Boolean.FALSE;

	@Override
	public Class<Movie> getType()
	{
		return Movie.class;
	}

	@Override
	public String doName(Movie movie, NamingService namingService, Map<String, Object> params)
	{
		// settings
		boolean includeYear = Namings.readParameter(params, PARAM_INCLUDE_YEAR_KEY, Boolean.class, PARAM_INCLUDE_YEAR_DEFAULT);

		Builder b = new Builder();
		b.append(Movie.PROP_NAME, movie.getTitleElseName());
		if (includeYear)
		{
			b.appendIfNotNull(Movie.PROP_DATE, movie.getYear());
		}
		return b.build();
	}
}
