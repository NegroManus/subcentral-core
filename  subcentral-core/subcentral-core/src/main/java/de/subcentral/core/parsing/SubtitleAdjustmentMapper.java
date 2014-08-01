package de.subcentral.core.parsing;

import java.time.temporal.Temporal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jsoup.helper.Validate;

import com.google.common.collect.ImmutableSet;

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

public class SubtitleAdjustmentMapper implements Mapper<SubtitleAdjustment>
{
	private PropParsingService	pps	= new PropParsingService();

	public PropParsingService getPropParsingService()
	{
		return pps;
	}

	public void setPropParsingService(PropParsingService pps)
	{
		Validate.notNull(pps, "pps cannot be null");
		this.pps = pps;
	}

	@Override
	public Class<SubtitleAdjustment> getType()
	{
		return SubtitleAdjustment.class;
	}

	@Override
	public Set<SimplePropDescriptor> getSupportedProperties()
	{
		return ImmutableSet.copyOf(new SimplePropDescriptor[] { Series.PROP_NAME, Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON,
				Episode.PROP_TITLE, Movie.PROP_NAME, Movie.PROP_TITLE, Movie.PROP_DATE, Release.PROP_TAGS, Release.PROP_GROUP,
				Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP, Subtitle.PROP_TAGS, Subtitle.PROP_SOURCE });
	}

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> info)
	{
		// Media
		AvMediaItem mediaItem = null;
		// If episode info is contained
		if (info.containsKey(Series.PROP_NAME))
		{
			Series series = new Series();
			series.setType(Series.TYPE_SEASONED);
			String name = info.get(Series.PROP_NAME);
			String title = info.get(Series.PROP_TITLE);
			series.setName(name);
			if (!Objects.equals(name, title))
			{
				series.setTitle(title);
			}
			series.setDate(pps.parse(info, Series.PROP_DATE, Temporal.class));
			series.setCountriesOfOrigin(pps.parseList(info, Series.PROP_COUNTRIES_OF_ORIGIN, String.class));
			Season season = series.newSeason();
			season.setNumber(pps.parse(info, Season.PROP_NUMBER, Integer.class));
			Episode epi = season.newEpisode();
			epi.setNumberInSeason(pps.parse(info, Episode.PROP_NUMBER_IN_SEASON, Integer.class));
			epi.setTitle(info.get(Episode.PROP_TITLE));
			mediaItem = epi;
		}
		// In movie info is contained
		else if (info.containsKey(Movie.PROP_NAME))
		{
			Movie mov = new Movie();
			mov.setName(info.get(Movie.PROP_NAME));
			mov.setTitle(info.get(Movie.PROP_TITLE));
			mov.setDate(pps.parse(info, Movie.PROP_DATE, Temporal.class));
			mediaItem = mov;
		}

		// Release
		Release rls = new Release();
		rls.getMedia().add(mediaItem);
		rls.setGroup(pps.parse(info, Release.PROP_GROUP, Group.class));
		rls.getTags().addAll(pps.parseList(info, Release.PROP_TAGS, Tag.class));

		// Subtitle
		Subtitle sub = new Subtitle();
		sub.setMediaItem(mediaItem);
		sub.setLanguage(info.get(Subtitle.PROP_LANGUAGE));
		sub.setGroup(pps.parse(info, Subtitle.PROP_GROUP, Group.class));
		sub.getTags().addAll(pps.parseList(info, Subtitle.PROP_TAGS, Tag.class));
		Subtitles.normalizeTags(sub);
		sub.setSource(info.get(Subtitle.PROP_SOURCE));

		// SubtitleAdjustment
		return sub.newAdjustment(rls);
	}
}
