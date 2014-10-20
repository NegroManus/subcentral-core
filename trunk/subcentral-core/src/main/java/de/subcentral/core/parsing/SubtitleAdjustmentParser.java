package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Splitter;

import de.subcentral.core.model.media.AvMedia;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.Standardizings;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentParser extends AbstractMappingParser<SubtitleAdjustment>
{
	private List<ConditionalMapper<List<? extends AvMedia>>>	mediaMappers				= new ArrayList<>(3);
	private Mapper<Release>										releaseMapper				= Parsings.getDefaultReleaseMapper();
	private Mapper<Subtitle>									subtitleMapper				= Parsings.getDefaultSubtitleMapper();
	private Mapper<SubtitleAdjustment>							subtitleAdjustmentMapper	= Parsings.getDefaultSubtitleAdjustmentMapper();

	public SubtitleAdjustmentParser(String domain)
	{
		super(domain);
		mediaMappers.add(new ConditionalMapper<List<? extends AvMedia>>(MultiEpisodeMapper::containsMultiEpisode,
				Parsings.getDefaultMultiEpisodeMapper()));
		mediaMappers.add(new ConditionalMapper<List<? extends AvMedia>>(props -> props.containsKey(Series.PROP_NAME),
				Parsings.createSingletonListMapper(Parsings.getDefaultEpisodeMapper())));
	}

	public List<ConditionalMapper<List<? extends AvMedia>>> getMediaMappers()
	{
		return mediaMappers;
	}

	public void setMediaMappers(List<ConditionalMapper<List<? extends AvMedia>>> mediaMappers)
	{
		this.mediaMappers = mediaMappers;
	}

	public Mapper<Release> getReleaseMapper()
	{
		return releaseMapper;
	}

	public void setReleaseMapper(Mapper<Release> releaseMapper)
	{
		this.releaseMapper = releaseMapper;
	}

	public Mapper<Subtitle> getSubtitleMapper()
	{
		return subtitleMapper;
	}

	public void setSubtitleMapper(Mapper<Subtitle> subtitleMapper)
	{
		this.subtitleMapper = subtitleMapper;
	}

	public Mapper<SubtitleAdjustment> getSubtitleAdjustmentMapper()
	{
		return subtitleAdjustmentMapper;
	}

	public void setSubtitleAdjustmentMapper(Mapper<SubtitleAdjustment> subtitleAdjustmentMapper)
	{
		this.subtitleAdjustmentMapper = subtitleAdjustmentMapper;
	}

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		List<? extends AvMedia> media = Parsings.tryMap(props, propParsingService, mediaMappers);

		// Release
		Set<Release> matchingRlss = parseMatchingReleases(props, media);

		// Subtitle
		List<Subtitle> subs = new ArrayList<>(media.size());
		for (AvMedia m : media)
		{
			Subtitle sub = subtitleMapper.map(props, propParsingService);
			sub.setMedia(m);
			subs.add(sub);
		}

		// SubtitleAdjustment
		SubtitleAdjustment subAdj = subtitleAdjustmentMapper.map(props, propParsingService);
		subAdj.getSubtitles().addAll(subs);
		subAdj.getMatchingReleases().addAll(matchingRlss);
		return subAdj;
	}

	private Set<Release> parseMatchingReleases(Map<SimplePropDescriptor, String> props, List<? extends AvMedia> media)
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
					Release rlsForGroup = releaseMapper.map(propsForRls, propParsingService);
					rlsForGroup.getMedia().addAll(media);
					Standardizings.mayStandardize(rlsForGroup, standardizingService);
					matchingReleases.add(rlsForGroup);
				}
				return matchingReleases;
			}
		}

		Release singleRelease = releaseMapper.map(props, propParsingService);
		singleRelease.getMedia().addAll(media);
		Standardizings.mayStandardize(singleRelease, standardizingService);

		matchingReleases = new HashSet<>(1);
		matchingReleases.add(singleRelease);

		return matchingReleases;
	}
}
