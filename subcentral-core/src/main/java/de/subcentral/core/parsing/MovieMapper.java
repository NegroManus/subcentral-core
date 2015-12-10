package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.util.SimplePropDescriptor;

public class MovieMapper extends AbstractMapper<Movie>
{
	public MovieMapper()
	{

	}

	public MovieMapper(PropFromStringService propFromStringService)
	{
		super(propFromStringService);
	}

	@Override
	public Movie doMap(Map<SimplePropDescriptor, String> props)
	{
		Movie mov = new Movie();
		mov.setName(props.get(Movie.PROP_NAME));
		mov.setTitle(props.get(Movie.PROP_TITLE));
		mov.setDate(propFromStringService.parse(props, Movie.PROP_DATE, Temporal.class));
		return mov;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Movie.class;
	}
}
