package de.subcentral.core.naming;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Media;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;
import de.subcentral.core.util.SeparationDefinition;

public class NamingStandards
{
	public static final String						DEFAULT_DOMAIN				= "default";
	private static final Function<String, String>	FINAL_FORMATTER				= initDefaultFinalFormatter();
	private static final SimplePropToStringService	PROP_TO_STRING_SERVICE		= new SimplePropToStringService();

	// NamingService has to be instantiated first because it is referenced in some namers
	private static final ConditionalNamingService	NAMING_SERVICE				= new ConditionalNamingService(DEFAULT_DOMAIN);
	private static final MediaNamer					MEDIA_NAMER					= new MediaNamer();
	private static final SeriesNamer				SERIES_NAMER				= new SeriesNamer();
	private static final SeasonNamer				SEASON_NAMER				= new SeasonNamer();
	private static final SeasonedEpisodeNamer		SEASONED_EPISODE_NAMER		= new SeasonedEpisodeNamer();
	private static final MiniSeriesEpisodeNamer		MINI_SERIES_EPISODE_NAMER	= new MiniSeriesEpisodeNamer();
	private static final DatedEpisodeNamer			DATED_EPISODE_NAMER			= new DatedEpisodeNamer();
	private static final EpisodeNamer				EPISODE_NAMER				= new EpisodeNamer();
	private static final MultiEpisodeNamer			MULTI_EPISODE_NAMER			= new MultiEpisodeNamer();
	private static final ReleaseNamer				RELEASE_NAMER				= new ReleaseNamer();
	private static final SubtitleNamer				SUBTITLE_NAMER				= new SubtitleNamer();
	private static final SubtitleAdjustmentNamer	SUBTITLE_ADJUSTMENT_NAMER	= new SubtitleAdjustmentNamer();
	static
	{
		// Configure namers

		// PropToStringService
		Function<Integer, String> epiNumToString = n -> String.format("E%02d", n);

		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(Year.class, (Year y) -> DateTimeFormatter.ofPattern("'('uuuu')'", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(YearMonth.class,
				(Year y) -> DateTimeFormatter.ofPattern("'('uuuu.MM')'", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDate.class,
				(LocalDate d) -> DateTimeFormatter.ofPattern("'('uuuu.MM.dd')'", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDateTime.class,
				(LocalDate d) -> DateTimeFormatter.ofPattern("'('uuuu.MM.dd.HH.mm.ss')'", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(ZonedDateTime.class,
				(ZonedDateTime d) -> DateTimeFormatter.ofPattern("'('uuuu.MM.dd.HH.mm.ss')'", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Season.PROP_NUMBER, (Integer n) -> String.format("S%02d", n));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SERIES, epiNumToString);
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SEASON, epiNumToString);

		// SeasonedEpisodeNamer
		SEASONED_EPISODE_NAMER.setPropToStringService(PROP_TO_STRING_SERVICE);
		SEASONED_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, "")));

		// EpisodeNamer
		ImmutableMap.Builder<String, Namer<Episode>> seriesTypeNamers = ImmutableMap.builder();
		seriesTypeNamers.put(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		seriesTypeNamers.put(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		seriesTypeNamers.put(Series.TYPE_DATED, DATED_EPISODE_NAMER);
		EPISODE_NAMER.setSeriesTypeNamers(seriesTypeNamers.build());

		// MultiEpisodeNamer
		MULTI_EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		MULTI_EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		MULTI_EPISODE_NAMER.registerSeriesTypeNamer(Series.TYPE_DATED, DATED_EPISODE_NAMER);
		MULTI_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""),
				SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SEASON, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SERIES, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				SeparationDefinition.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-")));

		// ReleaseNamer
		RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Release.PROP_GROUP, "-")));
		RELEASE_NAMER.setFinalFormatter(FINAL_FORMATTER);

		// SubtitleNamer
		ImmutableSet<SeparationDefinition> subtitleSeparations = ImmutableSet.of(SeparationDefinition.before(Subtitle.PROP_GROUP, "-"));
		SUBTITLE_NAMER.setSeparators(subtitleSeparations);

		// SubtitleReleaseNamer
		SUBTITLE_ADJUSTMENT_NAMER.setSeparators(subtitleSeparations);
		SUBTITLE_ADJUSTMENT_NAMER.setFinalFormatter(FINAL_FORMATTER);

		// Add namers to the NamingService
		List<ConditionalNamer<?>> namers = new ArrayList<>(8);
		namers.add(ConditionalNamer.create(RELEASE_NAMER, Release.class));
		namers.add(ConditionalNamer.create(SUBTITLE_ADJUSTMENT_NAMER, SubtitleAdjustment.class));
		namers.add(ConditionalNamer.create(EPISODE_NAMER, Episode.class));
		namers.add(ConditionalNamer.create(SERIES_NAMER, Series.class));
		namers.add(ConditionalNamer.create(SEASON_NAMER, Season.class));
		namers.add(ConditionalNamer.create(MULTI_EPISODE_NAMER, MultiEpisodeHelper::isMultiEpisode));
		namers.add(ConditionalNamer.create(MEDIA_NAMER, Media.class));
		namers.add(ConditionalNamer.create(SUBTITLE_NAMER, Subtitle.class));
		NAMING_SERVICE.getNamers().addAll(namers);
	}

	private static Function<String, String> initDefaultFinalFormatter()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-", "'´`", ".");
		return pr.andThen(cr);
	}

	public static Function<String, String> getDefaultWholeNameOperator()
	{
		return FINAL_FORMATTER;
	}

	public static PropToStringService getDefaultPropToStringService()
	{
		return PROP_TO_STRING_SERVICE;
	}

	public static NamingService getDefaultNamingService()
	{
		return NAMING_SERVICE;
	}

	public static Namer<Media> getDefaultMediaNamer()
	{
		return MEDIA_NAMER;
	}

	public static Namer<Episode> getDefaultEpisodeNamer()
	{
		return EPISODE_NAMER;
	}

	public static Namer<Episode> getDefaultSeasonedEpisodeNamer()
	{
		return SEASONED_EPISODE_NAMER;
	}

	public static Namer<Episode> getDefaultMiniSeriesEpisodeNamer()
	{
		return MINI_SERIES_EPISODE_NAMER;
	}

	public static Namer<Episode> getDefaultDatedEpisodeNamer()
	{
		return DATED_EPISODE_NAMER;
	}

	public static Namer<Series> getDefaultSeriesNamer()
	{
		return SERIES_NAMER;
	}

	public static Namer<Season> getDefaultSeasonNamer()
	{
		return SEASON_NAMER;
	}

	public static Namer<Collection<Episode>> getDefaultMultiEpisodeNamer()
	{
		return MULTI_EPISODE_NAMER;
	}

	public static Namer<Release> getDefaultReleaseNamer()
	{
		return RELEASE_NAMER;
	}

	public static Namer<Subtitle> getDefaultSubtitleNamer()
	{
		return SUBTITLE_NAMER;
	}

	public static Namer<SubtitleAdjustment> getDefaultSubtitleAdjustmentNamer()
	{
		return SUBTITLE_ADJUSTMENT_NAMER;
	}

	private NamingStandards()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}