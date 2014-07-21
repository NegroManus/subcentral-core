package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.util.CharReplacer;
import de.subcentral.core.util.SeparationDefinition;
import de.subcentral.core.util.SimplePropDescriptor;

public class NamingStandards
{
	public static final String					DEFAULT_DOMAIN			= "scene";
	public static final CharReplacer			STANDARD_REPLACER		= new CharReplacer();

	public static final SeriesNamer				SERIES_NAMER			= new SeriesNamer();
	public static final SeasonNamer				SEASON_NAMER			= new SeasonNamer();
	public static final SeasonedEpisodeNamer	SEASONED_EPISODE_NAMER	= new SeasonedEpisodeNamer();
	public static final MultiEpisodeNamer		MULTI_EPISODE_NAMER		= new MultiEpisodeNamer();
	public static final MovieNamer				MOVIE_NAMER				= new MovieNamer();
	public static final SubtitleNamer			SUBTITLE_NAMER			= new SubtitleNamer();
	public static final MediaReleaseNamer		MEDIA_RELEASE_NAMER		= new MediaReleaseNamer();
	public static final SubtitleReleaseNamer	SUBTITLE_RELEASE_NAMER	= new SubtitleReleaseNamer();
	public static final SimpleNamingService		NAMING_SERVICE			= new SimpleNamingService();
	static
	{
		Function<Integer, String> episodeNumberToString = n -> String.format("E%02d", n);
		Function<Integer, String> seasonNumberToString = n -> String.format("S%02d", n);

		SEASON_NAMER.setPropertyToStringFunctions(ImmutableMap.of(Season.PROP_NUMBER, seasonNumberToString));

		Map<SimplePropDescriptor, Function<?, String>> epiToStringFuncts = new HashMap<>();
		epiToStringFuncts.put(Episode.PROP_NUMBER_IN_SERIES, episodeNumberToString);
		epiToStringFuncts.put(Episode.PROP_NUMBER_IN_SEASON, episodeNumberToString);
		epiToStringFuncts.put(Season.PROP_NUMBER, seasonNumberToString);
		SEASONED_EPISODE_NAMER.setPropertyToStringFunctions(epiToStringFuncts);
		SEASONED_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.between(Season.PROP_NUMBER, Episode.PROP_NUMBER_IN_SEASON, "")));

		MULTI_EPISODE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SEASON,
				MultiEpisodeNamer.SEPARATION_TYPE_ADDITION,
				""),
				SeparationDefinition.inBetween(Episode.PROP_NUMBER_IN_SERIES, MultiEpisodeNamer.SEPARATION_TYPE_ADDITION, ""),
				SeparationDefinition.betweenAny(MultiEpisodeNamer.SEPARATION_TYPE_RANGE, "-")));

		MEDIA_RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Release.PROP_GROUP, "-")));
		MEDIA_RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		SUBTITLE_RELEASE_NAMER.setSeparators(ImmutableSet.of(SeparationDefinition.before(Subtitle.PROP_GROUP, "-")));
		SUBTITLE_RELEASE_NAMER.setWholeNameOperator(STANDARD_REPLACER);

		NAMING_SERVICE.setDomain(DEFAULT_DOMAIN);
		NAMING_SERVICE.registerNamer(SERIES_NAMER);
		NAMING_SERVICE.registerNamer(SEASONED_EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(SEASON_NAMER);
		NAMING_SERVICE.registerNamer(MULTI_EPISODE_NAMER);
		NAMING_SERVICE.registerNamer(MOVIE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_NAMER);
		NAMING_SERVICE.registerNamer(MEDIA_RELEASE_NAMER);
		NAMING_SERVICE.registerNamer(SUBTITLE_RELEASE_NAMER);
	}

	private NamingStandards()
	{
		// utility class
	}
}
