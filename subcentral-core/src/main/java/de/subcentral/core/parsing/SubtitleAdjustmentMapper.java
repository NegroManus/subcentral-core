package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper implements Mapper<SubtitleAdjustment>
{
	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		return new SubtitleAdjustment();
	}
}
