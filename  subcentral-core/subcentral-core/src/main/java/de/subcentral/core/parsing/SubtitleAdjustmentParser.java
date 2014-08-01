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
import de.subcentral.core.model.subtitle.Subtitles;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubtitleAdjustmentParser extends AbstractPropertyParser<SubtitleAdjustment>
{
	public SubtitleAdjustmentParser(String domain, List<MappingMatcher<SimplePropDescriptor>> matchers, PropParsingService pps)
	{
		super(domain, matchers, pps);
	}

	@Override
	public Class<SubtitleAdjustment> getTargetClass()
	{
		return SubtitleAdjustment.class;
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
			series.setType(Series.TYPE_SEASONED);
			String name = props.get(Series.PROP_NAME);
			String title = props.get(Series.PROP_TITLE);
			series.setName(name);
			if (!Objects.equals(name, title))
			{
				series.setTitle(title);
			}
			series.setDate(pps.parse(props, Series.PROP_DATE, Temporal.class));
			series.setCountriesOfOrigin(pps.parseList(props, Series.PROP_COUNTRIES_OF_ORIGIN, String.class));
			Season season = series.newSeason();
			season.setNumber(pps.parse(props, Season.PROP_NUMBER, Integer.class));
			Episode epi = season.newEpisode();
			epi.setNumberInSeason(pps.parse(props, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
			epi.setTitle(props.get(Episode.PROP_TITLE));
			mediaItem = epi;
		}
		// In movie info is contained
		else if (props.containsKey(Movie.PROP_NAME))
		{
			Movie mov = new Movie();
			mov.setName(props.get(Movie.PROP_NAME));
			mov.setTitle(props.get(Movie.PROP_TITLE));
			mov.setDate(pps.parse(props, Movie.PROP_DATE, Temporal.class));
			mediaItem = mov;
		}

		// Release
		Release rls = new Release();
		rls.getMedia().add(mediaItem);
		rls.setGroup(pps.parse(props, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(pps.parseList(props, Release.PROP_TAGS, Tag.class));

		// Subtitle
		Subtitle sub = new Subtitle();
		sub.setMediaItem(mediaItem);
		sub.setLanguage(props.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(pps.parse(props, Subtitle.PROP_GROUP, Group.class));
		sub.getTags().addAll(pps.parseList(props, Subtitle.PROP_TAGS, Tag.class));
		Subtitles.normalizeTags(sub);
		sub.setSource(props.get(Subtitle.PROP_SOURCE));

		// SubtitleAdjustment
		return sub.newAdjustment(rls);
	}
}
