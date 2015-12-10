package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleMapper extends AbstractMapper<Subtitle>
{
	public SubtitleMapper()
	{

	}

	public SubtitleMapper(PropFromStringService propFromStringService)
	{
		super(propFromStringService);
	}

	@Override
	public Subtitle doMap(Map<SimplePropDescriptor, String> props)
	{
		Subtitle sub = new Subtitle();
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(propFromStringService.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.setSource(props.get(Subtitle.PROP_SOURCE));
		return sub;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Subtitle.class;
	}
}
