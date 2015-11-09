package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleFile;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentMapper extends AbstractMapper<SubtitleFile>
{
	@Override
	public SubtitleFile doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService)
	{
		SubtitleFile subAdj = new SubtitleFile();
		subAdj.setName(props.get(SubtitleFile.PROP_NAME));
		subAdj.getTags().addAll(propFromStringService.parseList(props, SubtitleFile.PROP_TAGS, Tag.class));
		subAdj.setVersion(props.get(SubtitleFile.PROP_VERSION));
		return subAdj;
	}
}
