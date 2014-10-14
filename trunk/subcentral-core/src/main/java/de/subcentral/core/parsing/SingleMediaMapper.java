package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.model.media.SingleMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class SingleMediaMapper implements Mapper<SingleMedia>
{
	@Override
	public SingleMedia map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		SingleMedia media = new SingleMedia();
		media.setName(props.get(SingleMedia.PROP_NAME));
		media.setTitle(props.get(SingleMedia.PROP_TITLE));
		media.setDate(propParsingService.parse(props, SingleMedia.PROP_DATE, Temporal.class));
		return media;
	}
}
