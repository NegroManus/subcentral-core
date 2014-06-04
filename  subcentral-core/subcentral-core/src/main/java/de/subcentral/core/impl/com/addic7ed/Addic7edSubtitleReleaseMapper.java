package de.subcentral.core.impl.com.addic7ed;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Media;
import de.subcentral.core.media.Movie;
import de.subcentral.core.media.Season;
import de.subcentral.core.media.Series;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Releases;
import de.subcentral.core.release.Tag;
import de.subcentral.core.subtitle.Subtitle;
import de.subcentral.core.subtitle.SubtitleRelease;

/**
 * Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com
 * 
 * @author mhertram
 *
 */
public class Addic7edSubtitleReleaseMapper implements Mapper<SubtitleRelease>
{
	// Episode
	public final static String	SERIES_TITLE					= "series.title";
	public final static String	SEASON_NUMBER					= "season.number";
	public final static String	EPISODE_NUMBER					= "episode.number";
	public final static String	EPISODE_TITLE					= "episode.title";
	// Movie
	public final static String	MOVIE_TITLE						= "movie.title";
	public final static String	MOVIE_NAME						= "movie.name";
	public final static String	MOVIE_YEAR						= "movie.year";
	public final static String	MEDIA_RELEASE_TAGS				= "mediaRelease.tags";
	public final static String	MEDIA_RELEASE_GROUP				= "mediaRelease.group";
	public final static String	SUBTITLE_LANGUAGE				= "subtitle.language";
	public final static String	SUBTITLE_RELEASE_TAGS			= "subtitleRelease.tags";
	public final static String	SUBTITLE_RELEASE_GROUP			= "subtitleRelease.group";

	public final static String	DEFAULT_SUBTITLE_RELEASE_GROUP	= "addic7ed.com";

	private Splitter			tagSplitter						= Splitter.on(Pattern.compile("[^a-zA-Z0-9-]"));

	@Override
	public Class<SubtitleRelease> getType()
	{
		return SubtitleRelease.class;
	}

	@Override
	public String[] getKnownAttributeNames()
	{
		return new String[] { SERIES_TITLE, SEASON_NUMBER, EPISODE_NUMBER, EPISODE_TITLE, MOVIE_TITLE, MOVIE_NAME, MOVIE_YEAR, MEDIA_RELEASE_TAGS,
				MEDIA_RELEASE_GROUP, SUBTITLE_LANGUAGE, SUBTITLE_RELEASE_TAGS, SUBTITLE_RELEASE_GROUP };
	}

	@Override
	public SubtitleRelease map(Map<String, String> info)
	{
		String seriesTitle = info.get(SERIES_TITLE);
		String seasonNum = info.get(SEASON_NUMBER);
		String epiNum = info.get(EPISODE_NUMBER);
		String epiTitle = info.get(EPISODE_TITLE);

		String movieTitle = info.get(MOVIE_TITLE);
		String movieName = info.get(MOVIE_NAME);
		String movieYear = info.get(MOVIE_YEAR);

		String mediaRlsTags = info.get(MEDIA_RELEASE_TAGS);
		String mediaRlsGroup = info.get(MEDIA_RELEASE_GROUP);
		String subLang = info.get(SUBTITLE_LANGUAGE);
		String subRlsTags = info.get(SUBTITLE_RELEASE_TAGS);
		String subRlsGroup = info.get(SUBTITLE_RELEASE_GROUP);

		MediaRelease mediaRls = new MediaRelease();
		List<Media> media = new ArrayList<>();
		List<Subtitle> subs = new ArrayList<>();

		// If episode info is contained
		if (seriesTitle != null && (epiNum != null || epiTitle != null))
		{
			Series series = new Series();
			series.setTitle(seriesTitle);
			Season season = series.addSeason();
			if (seasonNum != null)
			{
				season.setNumber(Integer.parseInt(seasonNum));
			}
			Episode epi = season.addEpisode();
			if (epiNum != null)
			{
				epi.setNumberInSeason(Integer.parseInt(epiNum));
			}
			epi.setTitle(epiTitle);

			media.add(epi);

			Subtitle sub = new Subtitle();
			sub.setMedia(epi);
			sub.setLanguage(subLang);
			subs.add(sub);
		}
		if (movieTitle != null || movieName != null)
		{
			Movie movie = new Movie();
			movie.setTitle(movieTitle);
			movie.setName(movieName);
			if (movieYear != null)
			{
				movie.setDate(Year.parse(movieYear));
			}
			media.add(movie);

			Subtitle sub = new Subtitle();
			sub.setMedia(movie);
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
		return Releases.tagsOf(tagSplitter.splitToList(tags));
	}
}
