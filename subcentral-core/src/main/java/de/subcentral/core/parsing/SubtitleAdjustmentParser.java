package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Splitter;

import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentParser extends AbstractMappingParser<SubtitleAdjustment>
{
    private final Mapper<? extends List<? extends Media>> mediaMapper;
    private final Mapper<Release>			  releaseMapper		   = ParsingDefaults.getDefaultReleaseMapper();
    private final Mapper<Subtitle>			  subtitleMapper	   = ParsingDefaults.getDefaultSubtitleMapper();
    private final Mapper<SubtitleAdjustment>		  subtitleAdjustmentMapper = ParsingDefaults.getDefaultSubtitleAdjustmentMapper();

    public SubtitleAdjustmentParser(Mapper<? extends List<? extends Media>> mediaMapper)
    {
	this.mediaMapper = Objects.requireNonNull(mediaMapper, "mediaMapper");
    }

    public Mapper<? extends List<? extends Media>> getMediaMapper()
    {
	return mediaMapper;
    }

    public Mapper<Release> getReleaseMapper()
    {
	return releaseMapper;
    }

    public Mapper<Subtitle> getSubtitleMapper()
    {
	return subtitleMapper;
    }

    public Mapper<SubtitleAdjustment> getSubtitleAdjustmentMapper()
    {
	return subtitleAdjustmentMapper;
    }

    @Override
    public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props)
    {
	// Media
	List<? extends Media> media = mediaMapper.map(props, propFromStringService);

	// Release
	Set<Release> matchingRlss = parseMatchingReleases(props, media);

	// Subtitle
	List<Subtitle> subs = new ArrayList<>(media.size());
	for (Media m : media)
	{
	    Subtitle sub = subtitleMapper.map(props, propFromStringService);
	    sub.setMedia(m);
	    subs.add(sub);
	}

	// SubtitleAdjustment
	SubtitleAdjustment subAdj = subtitleAdjustmentMapper.map(props, propFromStringService);
	subAdj.getSubtitles().addAll(subs);
	subAdj.getMatchingReleases().addAll(matchingRlss);
	return subAdj;
    }

    private Set<Release> parseMatchingReleases(Map<SimplePropDescriptor, String> props, List<? extends Media> media)
    {
	Set<Release> matchingReleases;
	String groupStr = props.get(Release.PROP_GROUP);
	if (groupStr != null)
	{
	    // if the group string contains several groups separated by comma (e.g. "KILLERS, MSD")
	    List<String> groups = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(groupStr);
	    if (groups.size() > 1)
	    {
		matchingReleases = new HashSet<>(groups.size());
		for (String group : groups)
		{
		    Map<SimplePropDescriptor, String> propsForRls = new HashMap<>(props);
		    // overwrite the group value with the current group
		    propsForRls.put(Release.PROP_GROUP, group);
		    Release rlsForGroup = releaseMapper.map(propsForRls, propFromStringService);
		    rlsForGroup.getMedia().addAll(media);
		    matchingReleases.add(rlsForGroup);
		}
		return matchingReleases;
	    }
	}

	Release singleRelease = releaseMapper.map(props, propFromStringService);
	singleRelease.getMedia().addAll(media);

	matchingReleases = new HashSet<>(1);
	matchingReleases.add(singleRelease);

	return matchingReleases;
    }
}
