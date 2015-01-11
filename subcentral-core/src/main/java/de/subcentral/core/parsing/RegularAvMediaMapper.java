package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.model.media.RegularAvMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class RegularAvMediaMapper implements Mapper<RegularAvMedia>
{
	@Override
	public RegularAvMedia map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		RegularAvMedia media = new RegularAvMedia();
		media.setName(props.get(RegularAvMedia.PROP_NAME));
		media.setTitle(props.get(RegularAvMedia.PROP_TITLE));
		media.setDate(propFromStringService.parse(props, RegularAvMedia.PROP_DATE, Temporal.class));
		media.setMediaType(props.get(RegularAvMedia.PROP_MEDIA_TYPE));
		media.setMediaContentType(props.get(RegularAvMedia.PROP_MEDIA_CONTENT_TYPE));
		return media;
	}
}
