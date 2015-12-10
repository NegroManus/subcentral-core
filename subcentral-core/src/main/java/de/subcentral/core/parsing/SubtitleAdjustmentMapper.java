package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleRelease>
{
	public SubtitleAdjustmentMapper()
	{

	}

	public SubtitleAdjustmentMapper(PropFromStringService propFromStringService)
	{
		super(propFromStringService);
	}

	@Override
	public SubtitleRelease doMap(Map<SimplePropDescriptor, String> props)
	{
		SubtitleRelease subAdj = new SubtitleRelease();
		subAdj.setName(props.get(SubtitleRelease.PROP_NAME));
		subAdj.getTags().addAll(propFromStringService.parseList(props, SubtitleRelease.PROP_TAGS, Tag.class));
		subAdj.setVersion(props.get(SubtitleRelease.PROP_VERSION));
		return subAdj;
	}

	@Override
	protected Class<?> getTargetType()
	{
		return SubtitleRelease.class;
	}
}
