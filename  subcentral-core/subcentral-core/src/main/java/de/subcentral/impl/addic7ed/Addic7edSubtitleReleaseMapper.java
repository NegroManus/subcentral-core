package de.subcentral.impl.addic7ed;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleRelease;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.util.SimplePropertyDescriptor;

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
	public Set<SimplePropertyDescriptor> getKnownProperties()
	{
		return ImmutableSet.copyOf(new SimplePropertyDescriptor[] { Series.PROP_NAME, Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON,
				Episode.PROP_TITLE, Movie.PROP_NAME, Movie.PROP_TITLE, Movie.PROP_DATE, MediaRelease.PROP_TAGS, MediaRelease.PROP_GROUP,
				Subtitle.PROP_LANGUAGE, SubtitleRelease.PROP_TAGS, SubtitleRelease.PROP_GROUP });
	}

	@Override
	public SubtitleRelease map(Map<SimplePropertyDescriptor, String> info)
	{
		String seriesName = info.get(Series.PROP_NAME);
		String seasonNum = info.get(Season.PROP_NUMBER);
		String epiNum = info.get(Episode.PROP_NUMBER_IN_SEASON);
		String epiTitle = info.get(Episode.PROP_TITLE);

		String movieName = info.get(Movie.PROP_NAME);
		String movieTitle = info.get(Movie.PROP_TITLE);
		String movieYear = info.get(Movie.PROP_DATE);

		String mediaRlsTags = info.get(MediaRelease.PROP_TAGS);
		String mediaRlsGroup = info.get(MediaRelease.PROP_GROUP);
		String subLang = info.get(Subtitle.PROP_LANGUAGE);
		String subRlsTags = info.get(SubtitleRelease.PROP_TAGS);
		String subRlsGroup = info.get(SubtitleRelease.PROP_GROUP);

		MediaRelease mediaRls = new MediaRelease();
		List<Media> media = new ArrayList<>();
		List<Subtitle> subs = new ArrayList<>();

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

			media.add(epi);

			Subtitle sub = new Subtitle();
			sub.setMediaItem(epi);
			sub.setLanguage(subLang);
			subs.add(sub);
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
			media.add(movie);

			Subtitle sub = new Subtitle();
			sub.setMediaItem(movie);
			sub.setLanguage(subLang);
			subs.add(sub);
		}

		mediaRls.setMaterials(media);

		if (mediaRlsTags != null)
		{
			mediaRls.setTags(parseTags(mediaRlsTags));
		}
		if (mediaRlsGroup != null)
		{
			mediaRls.setGroup(new Group(mediaRlsGroup));
		}

		SubtitleRelease subRls = new SubtitleRelease();
		subRls.setMaterials(subs);
		subRls.setCompatibleMediaRelease(mediaRls);
		if (subRlsTags != null)
		{
			subRls.setTags(parseTags(subRlsTags));
		}
		if (subRlsGroup == null)
		{
			subRlsGroup = DEFAULT_SUBTITLE_RELEASE_GROUP;
		}
		subRls.setGroup(new Group(subRlsGroup));

		return subRls;
	}

	private List<Tag> parseTags(String tags)
	{
		return Releases.tags(tagSplitter.splitToList(tags));
	}
}
