package de.subcentral.core.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		Release rls = releaseMapper.map(props, propParsingService);
		rls.getMedia().add(mediaItem);
		Standardizings.mayStandardize(rls, standardizingService);

		// Subtitle
		Subtitle sub = subtitleMapper.map(props, propParsingService);
		sub.setMediaItem(mediaItem);
		Standardizings.mayStandardize(sub, standardizingService);

		// SubtitleAdjustment
		return Standardizings.mayStandardize(sub.newAdjustment(rls), standardizingService);
	}
}
