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

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.correction.CorrectionDefaults;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.metadata.media.NamedMedia;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.naming.ConditionalNamingService.ConditionalNamingEntry;
import de.subcentral.core.naming.PropSequenceNameBuilder.Config;
import de.subcentral.core.util.Predicates;
import de.subcentral.core.util.Separation;

public class NamingDefaults
{
	public static final String						DEFAULT_DOMAIN									= "default";

	private static final Function<String, String>	RELEASE_NAME_FORMATTER							= initReleaseNameFormatter();
	private static final Function<String, String>	RELEASE_MEDIA_FORMATTER							= initReleaseMediaFormatter();
	private static final Function<String, String>	SUBTITLE_RELEASE_NAME_FORMATTER					= initSubtitleReleaseNameFormatter();
	private static final Function<String, String>	NORMALIZING_FORMATTER							= initNormalizingFormatter();

	private static final SimplePropToStringService	PROP_TO_STRING_SERVICE							= new SimplePropToStringService();

	// NamingService has to be instantiated first because it is referenced in
	// some namers
	private static final ConditionalNamingService	NAMING_SERVICE									= new ConditionalNamingService(DEFAULT_DOMAIN);
	private static final DelegatingNamingService	RELEASE_MEDIA_NAMING_SERVICE					= new DelegatingNamingService(DEFAULT_DOMAIN + "_release_media",
																											NAMING_SERVICE,
																											RELEASE_MEDIA_FORMATTER);
	private static final DelegatingNamingService	NORMALIZING_NAMING_SERVICE						= createNormalizingNamingService(NAMING_SERVICE);
	private static final ConditionalNamingService	MULTI_EPISODE_RANGE_NAMING_SERVICE				= new ConditionalNamingService("multiepisode_range");;
	private static final DelegatingNamingService	MULTI_EPISODE_RANGE_NORMALIZING_NAMING_SERVICE	= createNormalizingNamingService(MULTI_EPISODE_RANGE_NAMING_SERVICE);
	private static MovieNamer						MOVIE_NAMER;
	private static SeriesNamer						SERIES_NAMER;
	private static SeasonNamer						SEASON_NAMER;
	private static EpisodeNamer						EPISODE_NAMER;
	private static MultiEpisodeNamer				MULTI_EPISODE_NAMER;
	private static MultiEpisodeNamer				MULTI_EPISODE_RANGE_NAMER;
	private static NamedMediaNamer					NAMED_MEDIA_NAMER;
	private static ReleaseNamer						RELEASE_NAMER;
	private static SubtitleNamer					SUBTITLE_NAMER;
	private static SubtitleReleaseNamer				SUBTITLE_RELEASE_NAMER;

	static
	{
		// Configure namers

		// PropToStringService
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(Year.class, (Year y) -> DateTimeFormatter.ofPattern("uuuu", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(YearMonth.class, (YearMonth y) -> DateTimeFormatter.ofPattern("uuuu.MM", Locale.US).format(y));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDate.class, (LocalDate d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(LocalDateTime.class, (LocalDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));
		PROP_TO_STRING_SERVICE.getTypeToStringFns().put(ZonedDateTime.class, (ZonedDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));

		// Episode
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Season.PROP_NUMBER, (Integer n) -> String.format("S%02d", n));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SERIES, (Integer n) -> String.format("E%02d", n));
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(Episode.PROP_NUMBER_IN_SEASON, (Integer n) -> String.format("E%02d", n));
		// Subtitle
		PROP_TO_STRING_SERVICE.getPropToStringFns().put(SubtitleRelease.PROP_VERSION, (String rev) -> "V" + rev);

		ImmutableSet.Builder<Separation> sepsBuilder = ImmutableSet.builder();
		sepsBuilder.add(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""));
		sepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""));
		sepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-"));
		sepsBuilder.add(Separation.before(Release.PROP_GROUP, "-"));
		sepsBuilder.add(Separation.before(Subtitle.PROP_GROUP, "-"));
		ImmutableSet<Separation> separations = sepsBuilder.build();

		ImmutableSet.Builder<Separation> multiEpisodeRangeSepsBuilder = ImmutableSet.builder();
		multiEpisodeRangeSepsBuilder.add(Separation.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, ""));
		multiEpisodeRangeSepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, "-"));
		multiEpisodeRangeSepsBuilder.add(Separation.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-"));
		ImmutableSet<Separation> multiEpisodeRangeSeparations = multiEpisodeRangeSepsBuilder.build();

		Config config = new Config(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, separations, null);
		Config configWithRlsNameFormatter = new Config(PROP_TO_STRING_SERVICE, ".", separations, RELEASE_NAME_FORMATTER);
		Config configWithSubRlsNameFormatter = new Config(PROP_TO_STRING_SERVICE, ".", separations, SUBTITLE_RELEASE_NAME_FORMATTER);
		Config configForMultiEpisodeRangeNamer = new Config(PROP_TO_STRING_SERVICE, Separation.DEFAULT_SEPARATOR, multiEpisodeRangeSeparations, null);

		// SeriesNamer
		SERIES_NAMER = new SeriesNamer(config);

		// SeasonNamer
		SEASON_NAMER = new SeasonNamer(config, SERIES_NAMER);

		// EpisodeNamer
		EPISODE_NAMER = new EpisodeNamer(config, SERIES_NAMER, SEASON_NAMER);

		// MultiEpisodeNamer
		MULTI_EPISODE_NAMER = new MultiEpisodeNamer(config, EPISODE_NAMER);

		// MutliEpisodeNamer with only range separations "-"
		MULTI_EPISODE_RANGE_NAMER = new MultiEpisodeNamer(configForMultiEpisodeRangeNamer, EPISODE_NAMER);

		// MovieNamer
		MOVIE_NAMER = new MovieNamer(config);

		// NamedMediaNamer
		NAMED_MEDIA_NAMER = new NamedMediaNamer(config);

		// ReleaseNamer
		RELEASE_NAMER = new ReleaseNamer(configWithRlsNameFormatter, RELEASE_MEDIA_NAMING_SERVICE);

		// SubtitleNamer
		SUBTITLE_NAMER = new SubtitleNamer(config, NAMING_SERVICE);

		// SubtitleReleaseNamer
		SUBTITLE_RELEASE_NAMER = new SubtitleReleaseNamer(configWithSubRlsNameFormatter, RELEASE_NAMER);

		// Add namers to the NamingService (ordered by number of times used)
		List<ConditionalNamingEntry<?>> namers = new ArrayList<>(9);
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Episode.class), EPISODE_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Release.class), RELEASE_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(SubtitleRelease.class), SUBTITLE_RELEASE_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Series.class), SERIES_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Season.class), SEASON_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Movie.class), MOVIE_NAMER));
		namers.add(ConditionalNamingEntry.of(MultiEpisodeHelper::isMultiEpisode, MULTI_EPISODE_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(Subtitle.class), SUBTITLE_NAMER));
		namers.add(ConditionalNamingEntry.of(Predicates.instanceOf(NamedMedia.class), NAMED_MEDIA_NAMER));
		NAMING_SERVICE.getConditionalNamingEntries().addAll(namers);

		// Add a special NamingService which formats the episode numbers different than the default NamingService
		// for ex. S09E23-E24 instead of S09E23E24
		MULTI_EPISODE_RANGE_NAMING_SERVICE.getConditionalNamingEntries().add(ConditionalNamingEntry.of(MultiEpisodeHelper::isMultiEpisode, MULTI_EPISODE_RANGE_NAMER));
	}

	private static Function<String, String> initReleaseNameFormatter()
	{
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER)
				.andThen(CorrectionDefaults.ALNUM_DOT_UNDERSCORE_HYPHEN_REPLACER)
				.andThen(CorrectionDefaults.DOT_HYPHEN_DOT_REPLACER);
	}

	private static Function<String, String> initReleaseMediaFormatter()
	{
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER).andThen(CorrectionDefaults.ALNUM_DOT_HYPEN_REPLACER).andThen(CorrectionDefaults.DOT_HYPHEN_DOT_REPLACER);
	}

	private static Function<String, String> initSubtitleReleaseNameFormatter()
	{
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.ALNUM_DOT_UNDERSCORE_HYPHEN_REPLACER).andThen(CorrectionDefaults.DOT_HYPHEN_DOT_REPLACER);
	}

	private static Function<String, String> initNormalizingFormatter()
	{
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER).andThen(CorrectionDefaults.ALNUM_BLANK_REPLACER).andThen(CorrectionDefaults.TO_LOWERCASE_REPLACER);
	}

	public static Function<String, String> getDefaultNormalizingFormatter()
	{
		return NORMALIZING_FORMATTER;
	}

	public static Function<String, String> getDefaultReleaseNameFormatter()
	{
		return RELEASE_NAME_FORMATTER;
	}

	public static Function<String, String> getDefaultReleaseMediaFormatter()
	{
		return RELEASE_MEDIA_FORMATTER;
	}

	public static Function<String, String> getDefaultSubtitleReleaseNameFormatter()
	{
		return SUBTITLE_RELEASE_NAME_FORMATTER;
	}

	public static PropToStringService getDefaultPropToStringService()
	{
		return PROP_TO_STRING_SERVICE;
	}

	public static NamingService getDefaultNamingService()
	{
		return NAMING_SERVICE;
	}

	public static DelegatingNamingService getDefaultReleaseMediaNamingService()
	{
		return RELEASE_MEDIA_NAMING_SERVICE;
	}

	public static ConditionalNamingService getMultiEpisodeRangeNamingService()
	{
		return MULTI_EPISODE_RANGE_NAMING_SERVICE;
	}

	public static DelegatingNamingService getDefaultNormalizingNamingService()
	{
		return NORMALIZING_NAMING_SERVICE;
	}

	public static DelegatingNamingService getMultiEpisodeRangeNormalizingNamingService()
	{
		return MULTI_EPISODE_RANGE_NORMALIZING_NAMING_SERVICE;
	}

	public static Namer<Movie> getDefaultMovieNamer()
	{
		return MOVIE_NAMER;
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

	public static Namer<List<? extends Episode>> getMultiEpisodeRangeNamer()
	{
		return MULTI_EPISODE_RANGE_NAMER;
	}

	public static Namer<Release> getDefaultReleaseNamer()
	{
		return RELEASE_NAMER;
	}

	public static Namer<Subtitle> getDefaultSubtitleNamer()
	{
		return SUBTITLE_NAMER;
	}

	public static Namer<SubtitleRelease> getDefaultSubtitleReleaseNamer()
	{
		return SUBTITLE_RELEASE_NAMER;
	}

	public static DelegatingNamingService createNormalizingNamingService(NamingService namingService)
	{
		return new DelegatingNamingService(namingService.getDomain() + "_normalizing", namingService, NORMALIZING_FORMATTER);
	}

	private NamingDefaults()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
