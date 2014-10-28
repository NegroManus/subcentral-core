package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;

import de.subcentral.core.model.media.SingleAvMedia;
import de.subcentral.core.model.media.SingleMedia;
import de.subcentral.core.util.SimplePropDescriptor;

public class SingleAvMediaMapper implements Mapper<SingleAvMedia>
{
	@Override
	public SingleAvMedia map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		SingleAvMedia media = new SingleAvMedia();
		media.setName(props.get(SingleAvMedia.PROP_NAME));
		media.setTitle(props.get(SingleAvMedia.PROP_TITLE));
		media.setDate(propFromStringService.parse(props, SingleMedia.PROP_DATE, Temporal.class));
		return media;
	}
}
