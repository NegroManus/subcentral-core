package de.subcentral.core.naming;

import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Locale;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.media.SingleMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
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
	public static final SubtitleNamer			SUBTITLE_NAMER				= new SubtitleNamer();
	public static final ReleaseNamer			RELEASE_NAMER				= new ReleaseNamer();
	public static final SubtitleAdjustmentNamer	SUBTITLE_ADJUSTMENT_NAMER	= new SubtitleAdjustmentNamer();
	static
	{
		// Configure namers
		Function<Integer, String> episodeNumberToString = n -> String.format("E%02d", n);
		Function<Integer, String> seasonNumberToString = n -> String.format("S%02d", n);
		Function<Temporal, String> yearToString = d -> DateTimeFormatter.ofPattern("'('uuuu')'", Locale.US).format(d);
		Function<Temporal, String> dateToString = d -> DateTimeFormatter.ofPattern("'('uuuu.MM.dd')'", Locale.US).format(d);

		// MediaNamer
		MEDIA_NAMER.setPropertyToStringFunctions(ImmutableMap.of(SingleMedia.PROP_DATE, yearToString));

		// Season
		SEASON_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Season.PROP_NUMBER, seasonNumberToString));

		// SeasonedEpisodeNamer
		ImmutableMap.Builder<SimplePropDescriptor, Function<?, String>> seasonedEpiToStringFns = ImmutableMap.builder();
		seasonedEpiToStringFns.put(Episode.PROP_NUMBER_IN_SERIES, episodeNumberToString);
		seasonedEpiToStringFns.put(Episode.PROP_NUMBER_IN_SEASON, episodeNumberToString);
		seasonedEpiToStringFns.put(Season.PROP_NUMBER, seasonNumberToString);
		SEASONED_EPISODE_NAMER.setPropertyToStringFunctions(seasonedEpiToStringFns.build());
		SEASONED_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, "")));

		// MiniSeriesEpisodeNamer
		MINI_SERIES_EPISODE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Episode.PROP_NUMBER_IN_SERIES, episodeNumberToString));

		// DatedEpisodeNamer
		DATED_EPISODE_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Episode.PROP_DATE, dateToString));

		// EpisodeNamer
		ImmutableMap.Builder<String, Namer<Episode>> seriesTypeNamers = ImmutableMap.builder();
		seriesTypeNamers.put(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		seriesTypeNamers.put(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		seriesTypeNamers.put(Series.TYPE_DATED, DATED_EPISODE_NAMER);
		EPISODE_NAMER.setSeriesTypeNamers(seriesTypeNamers.build());

		// MultiEpisodeNamer
		MULTI_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SEASON,
				MultiEpisodeNamer.SEPARATION_TYPE_ADDITION,
				""),
				SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SERIES, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				SeparationDefinition.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-")));

		// ReleaseNamer
		RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Release.PROP_GROUP, "-")));
		RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		// SubtitleReleaseNamer
		SUBTITLE_ADJUSTMENT_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Subtitle.PROP_GROUP, "-")));
		SUBTITLE_ADJUSTMENT_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		// Add namers to the NamingService
		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
		ImmutableMap.Builder<Class<?>, Namer<?>> allNamers = ImmutableMap.builder();
		allNamers.put(Media.class, MEDIA_NAMER);
		allNamers.put(Series.class, SERIES_NAMER);
		allNamers.put(Season.class, SEASON_NAMER);
		allNamers.put(Episode.class, EPISODE_NAMER);
		allNamers.put(MultiEpisodeHelper.class, MULTI_EPISODE_NAMER);
		allNamers.put(Subtitle.class, SUBTITLE_NAMER);
		allNamers.put(Release.class, RELEASE_NAMER);
		allNamers.put(SubtitleAdjustment.class, SUBTITLE_ADJUSTMENT_NAMER);
		NAMING_SERVICE.setNamers(allNamers.build());
	}

	public static Namer<Episode> getDefaultEpisodeNamer()
	{
		return EPISODE_NAMER;
	}

	public static Namer<Season> getDefaultSeasonNamer()
	{
		return SEASON_NAMER;
	}

	private NamingStandards()
	{
		// utility class
	}
}
