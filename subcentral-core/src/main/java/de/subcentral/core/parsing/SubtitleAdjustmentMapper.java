package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleAdjustment>
{
	@Override
	public SubtitleAdjustment doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		SubtitleAdjustment subAdj = new SubtitleAdjustment();
		subAdj.setName(props.get(SubtitleAdjustment.PROP_NAME));
		subAdj.getTags().addAll(propFromStringService.parseList(props, SubtitleAdjustment.PROP_TAGS, Tag.class));
		subAdj.setVersion(props.get(SubtitleAdjustment.PROP_VERSION));
		return subAdj;
	}
}
