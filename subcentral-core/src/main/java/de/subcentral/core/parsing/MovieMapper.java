package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.util.SimplePropDescriptor;

public class MovieMapper implements Mapper<Movie>
{
	@Override
	public Movie map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		Movie mov = new Movie();
		mov.setName(props.get(Movie.PROP_NAME));
		mov.setTitle(props.get(Movie.PROP_TITLE));
		mov.setDate(propFromStringService.parse(props, Movie.PROP_DATE, Temporal.class));
		return mov;
	}
}
