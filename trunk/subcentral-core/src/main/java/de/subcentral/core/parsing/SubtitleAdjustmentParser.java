package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.Standardizings;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.core.util.TimeUtil;

public class SubtitleAdjustmentParser extends AbstractMappingParser<SubtitleAdjustment>
{
	private List<ConditionalMapper<AvMediaItem>>	mediaItemMappers;
	private Mapper<Release>							releaseMapper;
	private Mapper<Subtitle>						subtitleMapper;
	private Mapper<SubtitleAdjustment>				subtitleAdjustmentMapper;

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

	public Mapper<SubtitleAdjustment> getSubtitleAdjustmentMapper()
	{
		return subtitleAdjustmentMapper;
	}

	public void setSubtitleAdjustmentMapper(Mapper<SubtitleAdjustment> subtitleAdjustmentMapper)
	{
		this.subtitleAdjustmentMapper = subtitleAdjustmentMapper;
	}

	public SubtitleAdjustmentParser(String domain)
	{
		super(domain);
	}

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> props)
	{
		// Media
		AvMediaItem mediaItem = null;
		// If episode info is contained
		if (props.containsKey(Series.PROP_NAME))
		{
			Series series = new Series();
			series.setType(pps.parse(props, Series.PROP_TYPE, String.class));
			String name = props.get(Series.PROP_NAME);
			String title = props.get(Series.PROP_TITLE);
			series.setName(name);
			if (!Objects.equals(name, title))
			{
				series.setTitle(title);
			}
			series.setDate(pps.parse(props, Series.PROP_DATE, Temporal.class));
			series.setCountriesOfOrigin(pps.parseList(props, Series.PROP_COUNTRIES_OF_ORIGIN, String.class));

			Episode epi = series.newEpisode();
			Parsings.reflectiveMapping(epi, props, pps);
			// epi.setNumberInSeries(pps.parse(props, Episode.PROP_NUMBER_IN_SERIES, Integer.class));
			// epi.setNumberInSeason(pps.parse(props, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
			// epi.setTitle(props.get(Episode.PROP_TITLE));
			// epi.setDate(pps.parse(props, Episode.PROP_DATE, LocalDate.class));

			if (props.containsKey(Season.PROP_NUMBER))
			{
				Season season = series.newSeason();
				season.setNumber(pps.parse(props, Season.PROP_NUMBER, Integer.class));
				epi.setSeason(season);
			}

			mediaItem = epi;
		}
		// If movie info is contained
		else if (props.containsKey(Movie.PROP_NAME))
		{
			Movie mov = new Movie();
			mov.setName(props.get(Movie.PROP_NAME));
			mov.setTitle(props.get(Movie.PROP_TITLE));
			mov.setDate(pps.parse(props, Movie.PROP_DATE, Temporal.class));
			mediaItem = mov;
		}
		Standardizings.mayStandardize(mediaItem, ss);

		// Release
		Release rls = new Release();
		rls.getMedia().add(mediaItem);
		rls.setGroup(pps.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(pps.parseList(props, Release.PROP_TAGS, Tag.class));
		rls.setSource(props.get(Release.PROP_SOURCE));
		rls.setSourceUrl(props.get(Release.PROP_SOURCE_URL));
		Standardizings.mayStandardize(rls, ss);

		// Subtitle
		Subtitle sub = new Subtitle();
		long start = System.nanoTime();
		Parsings.reflectiveMapping(sub, props, pps);
		TimeUtil.printDurationMillis(start);
		start = System.nanoTime();
		sub.setMediaItem(mediaItem);
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(pps.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.getTags().addAll(pps.parseList(props, Subtitle.PROP_TAGS, Tag.class));
		sub.setSource(props.get(Subtitle.PROP_SOURCE));
		sub.setSourceUrl(props.get(Subtitle.PROP_SOURCE_URL));
		TimeUtil.printDurationMillis(start);
		Standardizings.mayStandardize(sub, ss);

		// SubtitleAdjustment
		return Standardizings.mayStandardize(sub.newAdjustment(rls), ss);
	}
}
