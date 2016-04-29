package de.subcentral.core.parse;

import java.util.Map;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseMapper extends AbstractMapper<Release>
{
	public ReleaseMapper()
	{
		// default constructor
	}

	public ReleaseMapper(ParsePropService parsePropService)
	{
		super(parsePropService);
	}

	@Override
	public Release map(Map<SimplePropDescriptor, String> props)
	{
		Release rls = new Release();
		rls.setName(props.get(Release.PROP_NAME));
		rls.getTags().addAll(parsePropService.parseList(props, Release.PROP_TAGS, Tag.class));
		rls.setGroup(parsePropService.parse(props, Release.PROP_GROUP, Group.class));
		rls.setSource(parsePropService.parse(props, Release.PROP_SOURCE, Site.class));

		return rls;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Release.class;
	}
}
