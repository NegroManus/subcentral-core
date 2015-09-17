package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.GenericMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class GenericMediaMapper implements Mapper<GenericMedia>
{
	@Override
	public GenericMedia map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		GenericMedia media = new GenericMedia();
		media.setName(props.get(Movie.PROP_NAME));
		media.setTitle(props.get(Movie.PROP_TITLE));
		media.setDate(propFromStringService.parse(props, Movie.PROP_DATE, Temporal.class));
		media.setMediaType(props.get(Movie.PROP_MEDIA_TYPE));
		media.setMediaContentType(props.get(Movie.PROP_MEDIA_CONTENT_TYPE));
		return media;
	}
}
