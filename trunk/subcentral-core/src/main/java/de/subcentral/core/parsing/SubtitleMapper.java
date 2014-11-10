package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleMapper extends AbstractMapper<Subtitle>
{
	@Override
	public Subtitle doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		Subtitle sub = new Subtitle();
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.getTags().addAll(propFromStringService.parseList(props, Subtitle.PROP_TAGS, Tag.class));
		sub.setGroup(propFromStringService.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.setSource(props.get(Subtitle.PROP_SOURCE));
		return sub;
	}
}
