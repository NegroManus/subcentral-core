package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleMapper implements Mapper<Subtitle>
{
	@Override
	public Subtitle map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		Subtitle sub = new Subtitle();
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(propParsingService.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.getTags().addAll(propParsingService.parseList(props, Subtitle.PROP_TAGS, Tag.class));
		sub.setSource(props.get(Subtitle.PROP_SOURCE));
		sub.setSourceUrl(props.get(Subtitle.PROP_SOURCE_URL));
		return sub;
	}
}
