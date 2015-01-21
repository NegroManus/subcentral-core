package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseMapper extends AbstractMapper<Release>
{
	@Override
	public Release doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		Release rls = new Release();
		rls.setName(props.get(Release.PROP_NAME));
		rls.setGroup(propFromStringService.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(propFromStringService.parseList(props, Release.PROP_TAGS, Tag.class));
		return rls;
	}
}
