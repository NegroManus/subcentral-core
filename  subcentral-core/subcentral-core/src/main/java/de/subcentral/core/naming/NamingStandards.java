package de.subcentral.core.naming;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public class NamingStandards
{
	public static final String					DEFAULT_DOMAIN				= "scene";
	public static final CharReplacer			STANDARD_REPLACER			= new CharReplacer();

	// NamingService has to be instantiated first because it is referenced in some namers
	public static final SimpleNamingService		NAMING_SERVICE				= new SimpleNamingService();
	public static final MediaNamer				MEDIA_NAMER					= new MediaNamer();
	public static final SeriesNamer				SERIES_NAMER				= new SeriesNamer();
	public static final SeasonNamer				SEASON_NAMER				= new SeasonNamer();
	public static final SeasonedEpisodeNamer	SEASONED_EPISODE_NAMER		= new SeasonedEpisodeNamer();
	public static final MiniSeriesEpisodeNamer	MINI_SERIES_EPISODE_NAMER	= new MiniSeriesEpisodeNamer();
	public static final DatedEpisodeNamer		DATED_EPISODE_NAMER			= new DatedEpisodeNamer();
	public static final EpisodeNamer			EPISODE_NAMER				= new EpisodeNamer();
	public static final MultiEpisodeNamer		MULTI_EPISODE_NAMER			= new MultiEpisodeNamer();
	public static final MovieNamer				MOVIE_NAMER					= new MovieNamer();
	public static final SubtitleNamer			SUBTITLE_NAMER				= new SubtitleNamer();
	public static final ReleaseNamer			RELEASE_NAMER				= new ReleaseNamer();
	public static final SubtitleAdjustmentNamer	SUBTITLE_ADJUSTMENT_NAMER	= new SubtitleAdjustmentNamer();
	static
	{
		// Configure namers
		Function<Integer, String> episodeNumberToString = n -> String.format("E%02d", n);
		Function<Integer, String> seasonNumberToString = n -> String.format("S%02d", n);
		Function<Temporal, String> yearToString = d -> DateTimeFormatter.ofPattern("'('uuuu')'", Locale.US).format(d);
		Function<Temporal, String> dateToString = d -> DateTimeFormatter.ofPattern("'('uuuu-MM-dd')'", Locale.US).format(d);

		// Season
		SEASON_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Season.PROP_NUMBER, seasonNumberToString));

		// SeasonedEpisodeNamer
		Map<SimplePropDescriptor, Function<?, String>> epiToStringFuncts = new HashMap<>();
		epiToStringFuncts.put(Episode.PROP_NUMBER_IN_SERIES, episodeNumberToString);
		epiToStringFuncts.put(Episode.PROP_NUMBER_IN_SEASON, episodeNumberToString);
		epiToStringFuncts.put(Season.PROP_NUMBER, seasonNumberToString);
		SEASONED_EPISODE_NAMER.setPropertyToStringFunctions(epiToStringFuncts);
		SEASONED_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, "")));

		// MiniSeriesEpisodeNamer
		MINI_SERIES_EPISODE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Episode.PROP_NUMBER_IN_SERIES, episodeNumberToString));

		// DatedEpisodeNamer
		DATED_EPISODE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Episode.PROP_DATE, dateToString));

		// EpisodeNamer
		EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_DATED, DATED_EPISODE_NAMER);

		// MultiEpisodeNamer
		MULTI_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SEASON,
				MultiEpisodeNamer.SEPARATION_TYPE_ADDITION,
				""),
				SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SERIES, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				SeparationDefinition.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-")));

		// MovieNamer
		MOVIE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Movie.PROP_DATE, yearToString));

		// ReleaseNamer
		RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Release.PROP_GROUP, "-")));
		RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		// SubtitleReleaseNamer
		SUBTITLE_ADJUSTMENT_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Subtitle.PROP_GROUP, "-")));
		SUBTITLE_ADJUSTMENT_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		// Add namers to the NamingService
		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
		NAMING_SERVICE.registerNamer(MEDIA_NAMER);
		NAMING_SERVICE.registerNamer(SERIES_NAMER);
		NAMING_SERVICE.registerNamer(SEASON_NAMER);
		NAMING_SERVICE.registerNamer(EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(MULTI_EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(MOVIE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_NAMER);
		NAMING_SERVICE.registerNamer(RELEASE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_ADJUSTMENT_NAMER);
	}

	private NamingStandards()
	{
		// utility class
	}
}
