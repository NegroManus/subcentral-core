package de.subcentral.core.naming;

import java.util.Map;

import de.subcentral.core.model.media.Movie;

public class MovieNamer extends AbstractPropertySequenceNamer<Movie>
{
	@Override
	public Class<Movie> getType()
	{
		return Movie.class;
	}

	@Override
	public String doName(Movie movie, NamingService namingService, Map<String, Object> namingSettings)
	{
		return movie.getName();
	}
}
