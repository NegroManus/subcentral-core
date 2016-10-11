package de.subcentral.core.parse;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.metadata.media.GenericMedia;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.util.SimplePropDescriptor;

public class GenericMediaMapper extends AbstractMapper<GenericMedia> {
    @Override
    public GenericMedia map(Map<SimplePropDescriptor, String> props) {
        GenericMedia media = new GenericMedia();
        media.setName(props.get(Movie.PROP_NAME));
        media.setTitle(props.get(Movie.PROP_TITLE));
        media.setDate(parsePropService.parse(props, Movie.PROP_DATE, Temporal.class));
        media.setMediaType(props.get(Movie.PROP_MEDIA_TYPE));
        media.setMediaContentType(props.get(Movie.PROP_MEDIA_CONTENT_TYPE));
        return media;
    }

    @Override
    protected Class<?> getTargetType() {
        return GenericMedia.class;
    }
}
