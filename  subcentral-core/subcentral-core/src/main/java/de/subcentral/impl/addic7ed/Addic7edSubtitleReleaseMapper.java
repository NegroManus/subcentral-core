package de.subcentral.impl.addic7ed;

import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.AvMediaItem;
import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleRelease;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com
 * 
 * @author mhertram
 *
 */
public class Addic7edSubtitleReleaseMapper implements Mapper<SubtitleRelease>
{
	// Episode
	public final static String	DEFAULT_SUBTITLE_RELEASE_GROUP	= "addic7ed.com";

	private Splitter			tagSplitter						= Splitter.on(Pattern.compile("[^a-zA-Z0-9-]"));

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	@Override
	public Set<SimplePropDescriptor> getKnownProperties()
	{
		return ImmutableSet.copyOf(new SimplePropDescriptor[] { Series.PROP_NAME, Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON,
				Episode.PROP_TITLE, Movie.PROP_NAME, Movie.PROP_TITLE, Movie.PROP_DATE, Release.PROP_TAGS, Release.PROP_GROUP,
				Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP, Subtitle.PROP_TAGS });
	}

	@Override
	public SubtitleRelease map(Map<SimplePropDescriptor, String> info)
	{
		String seriesName = info.get(Series.PROP_NAME);
		String seasonNum = info.get(Season.PROP_NUMBER);
		String epiNum = info.get(Episode.PROP_NUMBER_IN_SEASON);
		String epiTitle = info.get(Episode.PROP_TITLE);

		String movieName = info.get(Movie.PROP_NAME);
		String movieTitle = info.get(Movie.PROP_TITLE);
		String movieYear = info.get(Movie.PROP_DATE);

		String mediaRlsTags = info.get(Release.PROP_TAGS);
		String mediaRlsGroup = info.get(Release.PROP_GROUP);
		String subLang = info.get(Subtitle.PROP_LANGUAGE);
		String subGroup = info.get(Subtitle.PROP_GROUP);
		String subTags = info.get(Subtitle.PROP_TAGS);

		// Media
		AvMediaItem mediaItem = null;
		// If episode info is contained
		if (seriesName != null && (epiNum != null || epiTitle != null))
		{
			Series series = new Series();
			series.setName(seriesName);
			Season season = series.newSeason();
			if (seasonNum != null)
			{
				season.setNumber(Integer.parseInt(seasonNum));
			}
			Episode epi = season.newEpisode();
			if (epiNum != null)
			{
				epi.setNumberInSeason(Integer.parseInt(epiNum));
			}
			epi.setTitle(epiTitle);
			mediaItem = epi;
		}
		if (movieTitle != null || movieName != null)
		{
			Movie movie = new Movie();
			movie.setName(movieName);
			movie.setTitle(movieTitle);
			if (movieYear != null)
			{
				movie.setDate(Year.parse(movieYear));
			}
			mediaItem = movie;
		}

		// MediaRelease
		Release mediaRls = new Release();
		mediaRls.setSingleMedia(mediaItem);
		if (mediaRlsTags != null)
		{
			mediaRls.setTags(parseTags(mediaRlsTags));
		}
		if (mediaRlsGroup != null)
		{
			mediaRls.setGroup(new Group(mediaRlsGroup));
		}

		// Subtitle
		Subtitle sub = new Subtitle();
		sub.setMediaItem(mediaItem);
		sub.setLanguage(subLang);
		if (subTags != null)
		{
			sub.setTags(parseTags(subTags));
		}
		if (subGroup == null)
		{
			subGroup = DEFAULT_SUBTITLE_RELEASE_GROUP;
		}
		sub.setGroup(new Group(subGroup));

		// SubtitleRelease
		SubtitleRelease subRls = new SubtitleRelease();
		subRls.setSingleSubtitle(sub);
		subRls.setSingleMatchingRelease(mediaRls);

		return subRls;
	}

	private List<Tag> parseTags(String tags)
	{
		return Releases.tags(tagSplitter.splitToList(tags));
	}
}
