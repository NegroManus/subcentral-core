package de.subcentral.core.naming;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.Subtitle.ForeignParts;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.PropSequenceNameBuilder.Config;
import de.subcentral.core.standardizing.CharStringReplacer;
import de.subcentral.core.standardizing.PatternMapStringReplacer;
import de.subcentral.core.util.Separation;

public class NamingDefaults
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
	private static EpisodeNamer						EPISODE_NAMER;
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
				(LocalDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(ZonedDateTime.class,
				(ZonedDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));

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

		ImmutableSet.Builder<Separation> sepsBuilder = ImmutableSet.builder();
		sepsBuilder.add(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""));
		sepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""));
		sepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-"));
		sepsBuilder.add(Separation.before(Release.PROP_GROUP, "-"));
		sepsBuilder.add(Separation.before(Subtitle.PROP_GROUP, "-"));
		ImmutableSet<Separation> separations = sepsBuilder.build();

		Config config = new Config(PROP_TO_STRING_SERVICE, separations, Separation.DEFAULT_SEPARATOR, null);
		Config configWithRlsNameFormatter = new Config(PROP_TO_STRING_SERVICE, separations, Separation.DEFAULT_SEPARATOR, RELEASE_MEDIA_FORMATTER);

		// EpisodeNamer
		EPISODE_NAMER = new EpisodeNamer(config);

		// MultiEpisodeNamer
		MULTI_EPISODE_NAMER = new MultiEpisodeNamer(config);

		// SeriesNamer
		SERIES_NAMER = new SeriesNamer(config);

		// SeasonNamer
		SEASON_NAMER = new SeasonNamer(config);

		// MediaNamer
		MEDIA_NAMER = new MediaNamer(config);

		// ReleaseNamer
		RELEASE_NAMER = new ReleaseNamer(configWithRlsNameFormatter, NAMING_SERVICE);

		// SubtitleNamer
		SUBTITLE_NAMER = new SubtitleNamer(config, NAMING_SERVICE);

		// SubtitleReleaseNamer
		SUBTITLE_ADJUSTMENT_NAMER = new SubtitleAdjustmentNamer(configWithRlsNameFormatter, RELEASE_NAMER);

		// Add namers to the NamingService
		List<ConditionalNamingEntry<?>> namers = new ArrayList<>(8);
		namers.add(ConditionalNamingEntry.of(SubtitleAdjustment.class, SUBTITLE_ADJUSTMENT_NAMER));
		namers.add(ConditionalNamingEntry.of(Release.class, RELEASE_NAMER));
		namers.add(ConditionalNamingEntry.of(Episode.class, EPISODE_NAMER));
		namers.add(ConditionalNamingEntry.of(Series.class, SERIES_NAMER));
		namers.add(ConditionalNamingEntry.of(Season.class, SEASON_NAMER));
		namers.add(ConditionalNamingEntry.of(Media.class, MEDIA_NAMER));
		namers.add(ConditionalNamingEntry.of(MultiEpisodeHelper::isMultiEpisode, MULTI_EPISODE_NAMER));
		namers.add(ConditionalNamingEntry.of(Subtitle.class, SUBTITLE_NAMER));
		NAMING_SERVICE.getConditionalNamingEntries().addAll(namers);
	}

	private static Function<String, String> initReleaseMediaFormatter()
	{
		PatternMapStringReplacer pr = new PatternMapStringReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		// hyphen "-" has to be allowed, so that media names like "How.I.Met.Your.Mother.S09E01-E24" are possible
		CharStringReplacer cr = new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.-", "'´`", '.');
		return pr.andThen(cr);
	}

	private static Function<String, String> initReleaseNameFormatter()
	{
		PatternMapStringReplacer pr = new PatternMapStringReplacer(ImmutableMap.of(Pattern.compile("&"), "and"));
		CharStringReplacer cr = new CharStringReplacer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-", "'´`", '.');
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

	public static Namer<Series> getDefaultSeriesNamer()
	{
		return SERIES_NAMER;
	}

	public static Namer<Season> getDefaultSeasonNamer()
	{
		return SEASON_NAMER;
	}

	public static Namer<List<? extends Episode>> getDefaultMultiEpisodeNamer()
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

	private NamingDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
