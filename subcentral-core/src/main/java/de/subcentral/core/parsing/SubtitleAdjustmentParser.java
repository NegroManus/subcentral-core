package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.Standardizings;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentParser extends AbstractMappingParser<SubtitleAdjustment>
{
	private List<ConditionalMapper<AvMediaItem>>	mediaItemMappers	= new ArrayList<>(2);
	private Mapper<Release>							releaseMapper		= Parsings.getDefaultReleaseMapper();
	private Mapper<Subtitle>						subtitleMapper		= Parsings.getDefaultSubtitleMapper();

	public SubtitleAdjustmentParser(String domain)
	{
		super(domain);
		mediaItemMappers.add(new ConditionalMapper<AvMediaItem>(props -> props.containsKey(Series.PROP_NAME), Parsings.getDefaultEpisodeMapper()));
		mediaItemMappers.add(new ConditionalMapper<AvMediaItem>(props -> props.containsKey(Movie.PROP_NAME), Parsings.getDefaultMovieMapper()));
	}

	public List<ConditionalMapper<AvMediaItem>> getMediaItemMappers()
	{
		return mediaItemMappers;
	}

	public void setMediaItemMappers(List<ConditionalMapper<AvMediaItem>> mediaItemMappers)
	{
		this.mediaItemMappers = mediaItemMappers;
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

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		AvMediaItem mediaItem = Parsings.tryMap(props, propParsingService, mediaItemMappers);
		Standardizings.mayStandardize(mediaItem, standardizingService);

		// Release
		Set<Release> matchingRlss = parseMatchingReleases(props, mediaItem);

		// Subtitle
		Subtitle sub = subtitleMapper.map(props, propParsingService);
		sub.setMediaItem(mediaItem);
		Standardizings.mayStandardize(sub, standardizingService);

		// SubtitleAdjustment
		SubtitleAdjustment subAdj = sub.newAdjustment(matchingRlss);
		Standardizings.mayStandardize(subAdj, standardizingService);
		return subAdj;
	}

	private Set<Release> parseMatchingReleases(Map<SimplePropDescriptor, String> props, AvMediaItem mediaItem)
	{
		Set<Release> matchingReleases;
		String groupStr = props.get(Release.PROP_GROUP);
		if (groupStr != null)
		{
			// if the group string contains several groups separated by comma (e.g. "KILLERS, MSD")
			List<String> groups = Splitter.on(Pattern.compile("[\\s,]+")).splitToList(groupStr);
			if (groups.size() > 1)
			{
				matchingReleases = new HashSet<>(groups.size());
				for (String group : groups)
				{
					Map<SimplePropDescriptor, String> propsForRls = new HashMap<>(props);
					// overwrite the group value with the current group
					propsForRls.put(Release.PROP_GROUP, group);
					Release rlsForGroup = releaseMapper.map(propsForRls, propParsingService);
					rlsForGroup.getMedia().add(mediaItem);
					Standardizings.mayStandardize(rlsForGroup, standardizingService);
					matchingReleases.add(rlsForGroup);
				}
				return matchingReleases;
			}
		}

		Release singleRelease = releaseMapper.map(props, propParsingService);
		singleRelease.getMedia().add(mediaItem);
		Standardizings.mayStandardize(singleRelease, standardizingService);

		matchingReleases = new HashSet<>(1);
		matchingReleases.add(singleRelease);

		return matchingReleases;
	}
}
