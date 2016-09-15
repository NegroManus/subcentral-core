package de.subcentral.core.name;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.correct.CorrectionDefaults;
import de.subcentral.core.metadata.NamedMetadata;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.metadata.media.Network;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Nuke;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.name.PropSequenceNameBuilder.Config;
import de.subcentral.core.util.Predicates;
import de.subcentral.core.util.Separation;
import de.subcentral.core.util.StringUtil;

public class NamingDefaults {
	public static final String						DEFAULT_DOMAIN									= "default";

	private static final Function<String, String>	RELEASE_NAME_FORMATTER							= initReleaseNameFormatter();
	private static final Function<String, String>	RELEASE_MEDIA_FORMATTER							= initReleaseMediaFormatter();
	private static final Function<String, String>	SUBTITLE_RELEASE_NAME_FORMATTER					= initSubtitleReleaseNameFormatter();
	private static final Function<String, String>	NORMALIZING_FORMATTER							= initNormalizingFormatter();

	private static final SimplePrintPropService		PRINT_PROP_SERVICE								= new SimplePrintPropService();

	// NamingService has to be instantiated first because it is referenced in
	// some namers
	private static final ConditionalNamingService	NAMING_SERVICE									= new ConditionalNamingService(DEFAULT_DOMAIN);
	private static final DecoratingNamingService	RELEASE_MEDIA_NAMING_SERVICE					= new DecoratingNamingService(DEFAULT_DOMAIN + "_release_media",
			NAMING_SERVICE,
			RELEASE_MEDIA_FORMATTER);
	private static final DecoratingNamingService	NORMALIZING_NAMING_SERVICE						= createNormalizingNamingService(NAMING_SERVICE);
	private static final ConditionalNamingService	MULTI_EPISODE_RANGE_NAMING_SERVICE				= new ConditionalNamingService("multiepisode_range");;
	private static final DecoratingNamingService	MULTI_EPISODE_RANGE_NORMALIZING_NAMING_SERVICE	= createNormalizingNamingService(MULTI_EPISODE_RANGE_NAMING_SERVICE);
	private static MovieNamer						MOVIE_NAMER;
	private static SeriesNamer						SERIES_NAMER;
	private static SeasonNamer						SEASON_NAMER;
	private static EpisodeNamer						EPISODE_NAMER;
	private static MultiEpisodeNamer				MULTI_EPISODE_NAMER;
	private static MultiEpisodeNamer				MULTI_EPISODE_RANGE_NAMER;
	private static NamedMetadataNamer				NAMED_MEDIA_NAMER;
	private static ReleaseNamer						RELEASE_NAMER;
	private static SubtitleNamer					SUBTITLE_NAMER;
	private static SubtitleReleaseNamer				SUBTITLE_RELEASE_NAMER;

	static {
		// Configure namers

		// PrintPropService
		// Type to string
		PRINT_PROP_SERVICE.getTypePrinter().put(Iterable.class, (Iterable<?> i) -> StringUtil.SPACE_JOINER.join(i));
		PRINT_PROP_SERVICE.getTypePrinter().put(Year.class, (Year y) -> DateTimeFormatter.ofPattern("uuuu", Locale.US).format(y));
		PRINT_PROP_SERVICE.getTypePrinter().put(YearMonth.class, (YearMonth y) -> DateTimeFormatter.ofPattern("uuuu.MM", Locale.US).format(y));
		PRINT_PROP_SERVICE.getTypePrinter().put(LocalDate.class, (LocalDate d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US).format(d));
		PRINT_PROP_SERVICE.getTypePrinter().put(LocalDateTime.class, (LocalDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));
		PRINT_PROP_SERVICE.getTypePrinter().put(ZonedDateTime.class, (ZonedDateTime d) -> DateTimeFormatter.ofPattern("uuuu.MM.dd_HH.mm.ss", Locale.US).format(d));

		PRINT_PROP_SERVICE.getTypePrinter().put(Tag.class, (Tag t) -> t.getName());
		PRINT_PROP_SERVICE.getTypePrinter().put(Group.class, (Group g) -> g.getName());
		PRINT_PROP_SERVICE.getTypePrinter().put(Nuke.class, (Nuke n) -> n.getReason());
		PRINT_PROP_SERVICE.getTypePrinter().put(Site.class, (Site s) -> s.getName());
		PRINT_PROP_SERVICE.getTypePrinter().put(Network.class, (Network n) -> n.getName());

		// Episode
		PRINT_PROP_SERVICE.getPropPrinter().put(Season.PROP_NUMBER, (Integer n) -> String.format("S%02d", n));
		PRINT_PROP_SERVICE.getPropPrinter().put(Episode.PROP_NUMBER_IN_SERIES, (Integer n) -> String.format("E%02d", n));
		PRINT_PROP_SERVICE.getPropPrinter().put(Episode.PROP_NUMBER_IN_SEASON, (Integer n) -> String.format("E%02d", n));
		// Subtitle
		PRINT_PROP_SERVICE.getPropPrinter().put(SubtitleRelease.PROP_VERSION, (String rev) -> "V" + rev);

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

		Config config = new Config(PRINT_PROP_SERVICE, Separation.DEFAULT_SEPARATOR, separations, null);
		Config configWithRlsNameFormatter = new Config(PRINT_PROP_SERVICE, ".", separations, RELEASE_NAME_FORMATTER);
		Config configWithSubRlsNameFormatter = new Config(PRINT_PROP_SERVICE, ".", separations, SUBTITLE_RELEASE_NAME_FORMATTER);
		Config configForMultiEpisodeRangeNamer = new Config(PRINT_PROP_SERVICE, Separation.DEFAULT_SEPARATOR, multiEpisodeRangeSeparations, null);

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

		// NamedMetadataNamer
		NAMED_MEDIA_NAMER = new NamedMetadataNamer(config);

		// ReleaseNamer
		RELEASE_NAMER = new ReleaseNamer(configWithRlsNameFormatter, RELEASE_MEDIA_NAMING_SERVICE);

		// SubtitleNamer
		SUBTITLE_NAMER = new SubtitleNamer(config, NAMING_SERVICE);

		// SubtitleReleaseNamer
		SUBTITLE_RELEASE_NAMER = new SubtitleReleaseNamer(configWithSubRlsNameFormatter, RELEASE_NAMER);

		// Add namers to the NamingService (mutual excluding conditions are ordered by estimated number of times used)
		NAMING_SERVICE.register(Predicates.instanceOf(Episode.class), EPISODE_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(Release.class), RELEASE_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(SubtitleRelease.class), SUBTITLE_RELEASE_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(Series.class), SERIES_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(Season.class), SEASON_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(Movie.class), MOVIE_NAMER);
		NAMING_SERVICE.register(MultiEpisodeHelper::isMultiEpisode, MULTI_EPISODE_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(Subtitle.class), SUBTITLE_NAMER);
		NAMING_SERVICE.register(Predicates.instanceOf(NamedMetadata.class), NAMED_MEDIA_NAMER);

		// Add a special NamingService which formats the episode numbers different than the default NamingService
		// for ex. S09E23-E24 instead of S09E23E24
		MULTI_EPISODE_RANGE_NAMING_SERVICE.register(MultiEpisodeHelper::isMultiEpisode, MULTI_EPISODE_RANGE_NAMER);
	}

	private NamingDefaults() {
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static Function<String, String> initReleaseNameFormatter() {
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER).andThen(CorrectionDefaults.ALNUM_DOT_UNDERSCORE_HYPHEN_REPLACER).andThen(CorrectionDefaults.HYPHEN_CLEANER);
	}

	private static Function<String, String> initReleaseMediaFormatter() {
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER).andThen(CorrectionDefaults.ALNUM_DOT_HYPEN_REPLACER).andThen(CorrectionDefaults.HYPHEN_CLEANER);
	}

	private static Function<String, String> initSubtitleReleaseNameFormatter() {
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.ALNUM_DOT_UNDERSCORE_HYPHEN_REPLACER).andThen(CorrectionDefaults.HYPHEN_CLEANER);
	}

	private static Function<String, String> initNormalizingFormatter() {
		return CorrectionDefaults.ACCENT_REPLACER.andThen(CorrectionDefaults.AND_REPLACER).andThen(CorrectionDefaults.ALNUM_BLANK_REPLACER).andThen(CorrectionDefaults.TO_LOWERCASE_REPLACER);
	}

	public static Function<String, String> getDefaultNormalizingFormatter() {
		return NORMALIZING_FORMATTER;
	}

	public static Function<String, String> getDefaultReleaseNameFormatter() {
		return RELEASE_NAME_FORMATTER;
	}

	public static Function<String, String> getDefaultReleaseMediaFormatter() {
		return RELEASE_MEDIA_FORMATTER;
	}

	public static Function<String, String> getDefaultSubtitleReleaseNameFormatter() {
		return SUBTITLE_RELEASE_NAME_FORMATTER;
	}

	public static PrintPropService getDefaultPrintPropService() {
		return PRINT_PROP_SERVICE;
	}

	public static NamingService getDefaultNamingService() {
		return NAMING_SERVICE;
	}

	public static DecoratingNamingService getDefaultReleaseMediaNamingService() {
		return RELEASE_MEDIA_NAMING_SERVICE;
	}

	public static ConditionalNamingService getMultiEpisodeRangeNamingService() {
		return MULTI_EPISODE_RANGE_NAMING_SERVICE;
	}

	public static DecoratingNamingService getDefaultNormalizingNamingService() {
		return NORMALIZING_NAMING_SERVICE;
	}

	public static DecoratingNamingService getMultiEpisodeRangeNormalizingNamingService() {
		return MULTI_EPISODE_RANGE_NORMALIZING_NAMING_SERVICE;
	}

	public static Namer<Movie> getDefaultMovieNamer() {
		return MOVIE_NAMER;
	}

	public static Namer<Episode> getDefaultEpisodeNamer() {
		return EPISODE_NAMER;
	}

	public static Namer<Series> getDefaultSeriesNamer() {
		return SERIES_NAMER;
	}

	public static Namer<Season> getDefaultSeasonNamer() {
		return SEASON_NAMER;
	}

	public static Namer<List<? extends Episode>> getDefaultMultiEpisodeNamer() {
		return MULTI_EPISODE_NAMER;
	}

	public static Namer<List<? extends Episode>> getMultiEpisodeRangeNamer() {
		return MULTI_EPISODE_RANGE_NAMER;
	}

	public static Namer<Release> getDefaultReleaseNamer() {
		return RELEASE_NAMER;
	}

	public static Namer<Subtitle> getDefaultSubtitleNamer() {
		return SUBTITLE_NAMER;
	}

	public static Namer<SubtitleRelease> getDefaultSubtitleReleaseNamer() {
		return SUBTITLE_RELEASE_NAMER;
	}

	public static DecoratingNamingService createNormalizingNamingService(NamingService namingService) {
		return new DecoratingNamingService(namingService.getName() + "_normalizing", namingService, NORMALIZING_FORMATTER);
	}
}