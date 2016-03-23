package de.subcentral.core.parse;

import java.util.Map;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleMapper extends AbstractMapper<Subtitle>
{
	public SubtitleMapper()
	{
		// default constructor
	}

	public SubtitleMapper(ParsePropService parsePropService)
	{
		super(parsePropService);
	}

	@Override
	public Subtitle doMap(Map<SimplePropDescriptor, String> props)
	{
		Subtitle sub = new Subtitle();
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(parsePropService.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.setSource(parsePropService.parse(props, Subtitle.PROP_SOURCE, Site.class));
		return sub;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Subtitle.class;
	}
}
