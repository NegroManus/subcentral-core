package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleAdjustment>
{
	public SubtitleAdjustmentMapper(PropParsingService propParsingService)
	{
		super(propParsingService);
	}

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props)
	{
		SubtitleAdjustment subAdj = new SubtitleAdjustment();
		return subAdj;
	}
}
