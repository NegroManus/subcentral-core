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
import de.subcentral.core.model.subtitle.Subtitle.ForeignParts;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.PatternReplacer;
import de.subcentral.core.util.Separation;

public class NamingStandards
{
	public static final String						DEFAULT_DOMAIN			= "default";
	private static final Function<String, String>	RELEASE_NAME_FORMATTER	= initReleaseNameFormatter();
	private static final Function<String, String>	RELEASE_MEDIA_FORMATTER	= initReleaseMediaFormatter();

	private static final SimplePropToStringService	PROP_TO_STRING_SERVICE	= new SimplePropToStringService();

	// NamingService has to be instantiated first because it is referenced in some namers
	private static final ConditionalNamingService	NAMING_SERVICE			= new ConditionalNamingService(DEFAULT_DOMAIN);
	private static MediaNamer						MEDIA_NAMER;
	private static SeriesNamer						SERIES_NAMER;
	private static SeasonNamer						SEASON_NAMER;
	private static SeasonedEpisodeNamer				SEASONED_EPISODE_NAMER;
	private static MiniSeriesEpisodeNamer			MINI_SERIES_EPISODE_NAMER;
	private static DatedEpisodeNamer				DATED_EPISODE_NAMER;
	private static SeriesTypeDependentEpisodeNamer	EPISODE_NAMER;
	private static MultiEpisodeNamer				MULTI_EPISODE_NAMER;
	private static ReleaseNamer						RELEASE_NAMER;
	private static SubtitleNamer					SUBTITLE_NAMER;
	private static SubtitleAdjustmentNamer			SUBTITLE_ADJUSTMENT_NAMER;
	static
	{
		// Configure namers

		// PropToStringService
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(Year.class, (Year y) -> DateTimeFormatter.ofPattern("uuuu", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns()
				.put(YearMonth.class, (YearMonth y) -> DateTimeFormatter.ofPattern("uuuu.MM", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDate.class,
				(LocalDate d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDateTime.class,
				(LocalDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(ZonedDateTime.class,
				(ZonedDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd.HH.mm.ss", Locale.US).format(d));

		// Episode
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Season.PROP_NUMBER, (Integer n) -> String.format("S%02d", n));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SERIES, (Integer n) -> String.format("E%02d", n));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SEASON, (Integer n) -> String.format("E%02d", n));
		// Release
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Release.PROP_MEDIA, RELEASE_MEDIA_FORMATTER);
		// Subtitle
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Subtitle.PROP_HEARING_IMPAIRED, (Boolean hi) -> hi ? "HI" : "");
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Subtitle.PROP_FOREIGN_PARTS,
				(ForeignParts fp) -> fp == ForeignParts.NONE ? "" : "FOREIGN_PARTS_" + fp.name());
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Subtitle.PROP_VERSION, (String version) -> "V" + version);

		// DatedEpisodeNamer
		DATED_EPISODE_NAMER = new DatedEpisodeNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, ImmutableSet.of(), null);

		MINI_SERIES_EPISODE_NAMER = new MiniSeriesEpisodeNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, ImmutableSet.of(), null);

		// SeasonedEpisodeNamer
		ImmutableSet<Separation> seasonedEpiSeps = ImmutableSet.of(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""));
		SEASONED_EPISODE_NAMER = new SeasonedEpisodeNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, seasonedEpiSeps, null);

		// EpisodeNamer
		ImmutableMap.Builder<String, Namer<Episode>> epiSeriesTypeNamers = ImmutableMap.builder();
		epiSeriesTypeNamers.put(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		epiSeriesTypeNamers.put(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		epiSeriesTypeNamers.put(Series.TYPE_DATED, DATED_EPISODE_NAMER);
		EPISODE_NAMER = new SeriesTypeDependentEpisodeNamer(epiSeriesTypeNamers.build(), SEASONED_EPISODE_NAMER);

		// MultiEpisodeNamer
		ImmutableSet<Separation> multiEpiSeps = ImmutableSet.of(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""),
				Separation.inBetween(Episode.PROP_NUMBER_IN_SEASON, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				Separation.inBetween(Episode.PROP_NUMBER_IN_SERIES, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-"));
		ImmutableMap.Builder<String, AbstractEpisodeNamer> multiEpiSeriesTypeNamers = ImmutableMap.builder();
		multiEpiSeriesTypeNamers.put(Series.TYPE_SEASONED, SEASONED_EPISODE_NAMER);
		multiEpiSeriesTypeNamers.put(Series.TYPE_MINI_SERIES, MINI_SERIES_EPISODE_NAMER);
		multiEpiSeriesTypeNamers.put(Series.TYPE_DATED, DATED_EPISODE_NAMER);
		MULTI_EPISODE_NAMER = new MultiEpisodeNamer(PROP_TO_STRING_SERVICE,
				Separation.DEFAULT_SEPARATOR,
				multiEpiSeps,
				null,
				multiEpiSeriesTypeNamers.build(),
				SEASONED_EPISODE_NAMER);

		// SeriesNamer
		SERIES_NAMER = new SeriesNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, ImmutableSet.of(), null);

		// SeasonNamer
		SEASON_NAMER = new SeasonNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, ImmutableSet.of(), null);

		// MediaNamer
		MEDIA_NAMER = new MediaNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, ImmutableSet.of(), null);

		// ReleaseNamer
		ImmutableSet<Separation> rlsSeps = ImmutableSet.of(Separation.before(Release.PROP_GROUP, "-"));
		RELEASE_NAMER = new ReleaseNamer(PROP_TO_STRING_SERVICE, ".", rlsSeps, RELEASE_NAME_FORMATTER, NAMING_SERVICE);

		// SubtitleNamer
		ImmutableSet<Separation> subtitleSeps = ImmutableSet.of(Separation.before(Subtitle.PROP_GROUP, "-"));
		SUBTITLE_NAMER = new SubtitleNamer(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, subtitleSeps, null, NAMING_SERVICE);

		// SubtitleReleaseNamer
		SUBTITLE_ADJUSTMENT_NAMER = new SubtitleAdjustmentNamer(PROP_TO_STRING_SERVICE, ".", subtitleSeps, RELEASE_NAME_FORMATTER, RELEASE_NAMER);

		// Add namers to the NamingService
		List<ConditionalNamer<?>> namers = new ArrayList<>(8);
		namers.add(ConditionalNamer.create(SUBTITLE_ADJUSTMENT_NAMER, SubtitleAdjustment.class));
		namers.add(ConditionalNamer.create(RELEASE_NAMER, Release.class));
		namers.add(ConditionalNamer.create(EPISODE_NAMER, Episode.class));
		namers.add(ConditionalNamer.create(SERIES_NAMER, Series.class));
		namers.add(ConditionalNamer.create(SEASON_NAMER, Season.class));
		namers.add(ConditionalNamer.create(MEDIA_NAMER, Media.class));
		namers.add(ConditionalNamer.create(MULTI_EPISODE_NAMER, MultiEpisodeHelper::isMultiEpisode));
		namers.add(ConditionalNamer.create(SUBTITLE_NAMER, Subtitle.class));
		NAMING_SERVICE.getNamers().addAll(namers);
	}

	private static Function<String, String> initReleaseMediaFormatter()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		// hyphen "-" has to be allowed, so that media names like "How.I.Met.Your.Mother.S09E01-E24" are possible
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-", "'´`", '.');
		return pr.andThen(cr);
	}

	private static Function<String, String> initReleaseNameFormatter()
	{
		PatternReplacer pr = new PatternReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharReplacer cr = new CharReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-", "'´`", '.');
		return pr.andThen(cr);
	}

	public static Function<String, String> getDefaultReleaseNameFormatter()
	{
		return RELEASE_NAME_FORMATTER;
	}

	public static Function<String, String> getDefaultReleaseMediaFormatter()
	{
		return RELEASE_MEDIA_FORMATTER;
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

	public static Namer<Collection<? extends Episode>> getDefaultMultiEpisodeNamer()
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
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
