package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
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
		rls.setSource(propFromStringService.parse(props, Release.PROP_SOURCE, String.class));
		rls.setSourceUrl(propFromStringService.parse(props, Release.PROP_SOURCE_URL, String.class));
		return rls;
	}
}
