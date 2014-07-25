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
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.util.SimplePropDescriptor;

/**
 * Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com
 * 
 * @author mhertram
 *
 */
public class Addic7edSubtitleAdjustmentMapper implements Mapper<SubtitleAdjustment>
{
	// Episode
	public final static String	DEFAULT_SUBTITLE_SOURCE	= "Addic7ed.com";

	private Splitter			tagSplitter				= Splitter.on(Pattern.compile("[^a-zA-Z0-9-]"));

	@Override
	public Class<SubtitleAdjustment> getType()
	{
		return SubtitleAdjustment.class;
	}

	@Override
	public Set<SimplePropDescriptor> getKnownProperties()
	{
		return ImmutableSet.copyOf(new SimplePropDescriptor[] { Series.PROP_NAME, Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON,
				Episode.PROP_TITLE, Movie.PROP_NAME, Movie.PROP_TITLE, Movie.PROP_DATE, Release.PROP_TAGS, Release.PROP_GROUP,
				Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP, Subtitle.PROP_TAGS });
	}

	@Override
	public SubtitleAdjustment map(Map<SimplePropDescriptor, String> info)
	{
		String seriesName = info.get(Series.PROP_NAME);
		String seasonNum = info.get(Season.PROP_NUMBER);
		String epiNum = info.get(Episode.PROP_NUMBER_IN_SEASON);
		String epiTitle = info.get(Episode.PROP_TITLE);

		String movieName = info.get(Movie.PROP_NAME);
		String movieTitle = info.get(Movie.PROP_TITLE);
		String movieYear = info.get(Movie.PROP_DATE);

		String rlsTags = info.get(Release.PROP_TAGS);
		String rlsGroup = info.get(Release.PROP_GROUP);
		String subLang = info.get(Subtitle.PROP_LANGUAGE);
		String subSrc = info.get(Subtitle.PROP_SOURCE);
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
		Release rls = new Release();
		rls.setSingleMedia(mediaItem);
		if (rlsTags != null)
		{
			rls.setTags(parseTags(rlsTags));
		}
		if (rlsGroup != null)
		{
			rls.setGroup(new Group(rlsGroup));
		}

		// Subtitle
		Subtitle sub = new Subtitle();
		sub.setMediaItem(mediaItem);
		sub.setLanguage(subLang);
		if (subTags != null)
		{
			sub.getTags().addAll(parseTags(subTags));
		}
		if (subSrc == null)
		{
			subSrc = DEFAULT_SUBTITLE_SOURCE;
		}
		sub.setSource(subSrc);

		// SubtitleRelease
		SubtitleAdjustment subRls = new SubtitleAdjustment();
		subRls.getSubtitles().add(sub);
		subRls.getMatchingReleases().add(rls);

		return subRls;
	}

	private List<Tag> parseTags(String tags)
	{
		return Releases.tags(tagSplitter.splitToList(tags));
	}
}
