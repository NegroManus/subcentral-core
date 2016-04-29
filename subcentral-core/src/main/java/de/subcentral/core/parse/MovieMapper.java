package de.subcentral.core.parse;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.util.SimplePropDescriptor;

public class MovieMapper extends AbstractMapper<Movie>
{
	public MovieMapper()
	{
		// default constructor
	}

	public MovieMapper(ParsePropService parsePropService)
	{
		super(parsePropService);
	}

	@Override
	public Movie map(Map<SimplePropDescriptor, String> props)
	{
		Movie mov = new Movie();
		mov.setName(props.get(Movie.PROP_NAME));
		mov.setTitle(props.get(Movie.PROP_TITLE));
		mov.setDate(parsePropService.parse(props, Movie.PROP_DATE, Temporal.class));
		return mov;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Movie.class;
	}
}
