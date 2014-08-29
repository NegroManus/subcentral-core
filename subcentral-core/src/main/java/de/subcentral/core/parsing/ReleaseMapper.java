package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.util.SimplePropDescriptor;

public class ReleaseMapper extends AbstractMapper<Release>
{
	@Override
	public Release doMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		Release rls = new Release();
		rls.setName(props.get(Release.PROP_NAME));
		rls.setGroup(propParsingService.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(propParsingService.parseList(props, Release.PROP_TAGS, Tag.class));
		rls.setSource(propParsingService.parse(props, Release.PROP_SOURCE, String.class));
		rls.setSourceUrl(propParsingService.parse(props, Release.PROP_SOURCE_URL, String.class));
		return rls;
	}
}