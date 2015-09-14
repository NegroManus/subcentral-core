package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.SimpleMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class SimpleMediaMapper implements Mapper<SimpleMedia>
{
	@Override
	public SimpleMedia map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		SimpleMedia media = new SimpleMedia();
		media.setName(props.get(Movie.PROP_NAME));
		media.setTitle(props.get(Movie.PROP_TITLE));
		media.setDate(propFromStringService.parse(props, Movie.PROP_DATE, Temporal.class));
		media.setMediaType(props.get(Movie.PROP_MEDIA_TYPE));
		media.setMediaContentType(props.get(Movie.PROP_MEDIA_CONTENT_TYPE));
		return media;
	}
}
