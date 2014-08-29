package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleAdjustment>
{
	@Override
	public SubtitleAdjustment doMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		SubtitleAdjustment subAdj = new SubtitleAdjustment();
		subAdj.setName(props.get(SubtitleAdjustment.PROP_NAME));
		return subAdj;
	}
}
