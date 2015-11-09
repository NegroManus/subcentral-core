package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleVariant;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleVariant>
{
	@Override
	public SubtitleVariant doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		SubtitleVariant subAdj = new SubtitleVariant();
		subAdj.setName(props.get(SubtitleVariant.PROP_NAME));
		subAdj.getTags().addAll(propFromStringService.parseList(props, SubtitleVariant.PROP_TAGS, Tag.class));
		subAdj.setVersion(props.get(SubtitleVariant.PROP_VERSION));
		return subAdj;
	}
}
