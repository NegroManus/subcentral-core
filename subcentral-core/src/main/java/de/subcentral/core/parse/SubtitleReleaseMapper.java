package de.subcentral.core.parse;

import java.util.Map;

import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleReleaseMapper extends AbstractMapper<SubtitleRelease> {
    public SubtitleReleaseMapper() {
        // default constructor
    }

    public SubtitleReleaseMapper(ParsePropService parsePropService) {
        super(parsePropService);
    }

    @Override
    public SubtitleRelease map(Map<SimplePropDescriptor, String> props) {
        SubtitleRelease subAdj = new SubtitleRelease();
        subAdj.setName(props.get(SubtitleRelease.PROP_NAME));
        subAdj.getTags().addAll(parsePropService.parseList(props, SubtitleRelease.PROP_TAGS, Tag.class));
        subAdj.setVersion(props.get(SubtitleRelease.PROP_VERSION));
        return subAdj;
    }

    @Override
    protected Class<?> getTargetType() {
        return SubtitleRelease.class;
    }
}
