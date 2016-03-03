package de.subcentral.core.parse;

import java.util.Map;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseMapper extends AbstractMapper<Release>
{
	public ReleaseMapper()
	{

	}

	public ReleaseMapper(ParsePropService parsePropService)
	{
		super(parsePropService);
	}

	@Override
	public Release doMap(Map<SimplePropDescriptor, String> props)
	{
		Release rls = new Release();
		rls.setName(props.get(Release.PROP_NAME));
		rls.setGroup(parsePropService.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(parsePropService.parseList(props, Release.PROP_TAGS, Tag.class));
		return rls;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return Release.class;
	}
}
