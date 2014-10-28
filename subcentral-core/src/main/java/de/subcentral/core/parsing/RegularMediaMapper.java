package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.model.media.RegularMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class RegularMediaMapper implements Mapper<RegularMedia>
{
	@Override
	public RegularMedia map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		RegularMedia media = new RegularMedia();
		media.setName(props.get(RegularMedia.PROP_NAME));
		media.setTitle(props.get(RegularMedia.PROP_TITLE));
		media.setDate(propFromStringService.parse(props, RegularMedia.PROP_DATE, Temporal.class));
		media.setMediaType(props.get(RegularMedia.PROP_MEDIA_TYPE));
		media.setMediaContentType(props.get(RegularMedia.PROP_MEDIA_CONTENT_TYPE));
		return media;
	}
}
