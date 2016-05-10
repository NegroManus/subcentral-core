package de.subcentral.support.italiansubsnet;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.MappingMatcher;
import de.subcentral.core.parse.MultiMappingMatcher;
import de.subcentral.core.parse.ParsingDefaults;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.parse.PatternMappingMatcher;
import de.subcentral.core.parse.SubtitleReleaseParser;
import de.subcentral.core.parse.TypeBasedParsingService;
import de.subcentral.core.parse.TypeBasedParsingService.ParserEntry;
import de.subcentral.core.util.SimplePropDescriptor;

public class ItalianSubsNet
{
	private static final Site						SITE			= new Site("italiansubs.net", "italiansubs.net", "http://www.italiansubs.net/");
	private static final TypeBasedParsingService	PARSING_SERVICE	= initParsingService();

	private ItalianSubsNet()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static TypeBasedParsingService initParsingService()
	{
		ImmutableMap.Builder<SimplePropDescriptor, String> commonPredefMatchesBuilder = ImmutableMap.builder();
		commonPredefMatchesBuilder.put(Subtitle.PROP_SOURCE, getSite().getName());
		commonPredefMatchesBuilder.put(Subtitle.PROP_LANGUAGE, Locale.ITALIAN.toString());
		ImmutableMap<SimplePropDescriptor, String> commonPredefMatches = commonPredefMatchesBuilder.build();

		// --------------
		// Episode Parsers

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> episodeMatchers = ImmutableList.builder();

		// Examples:
		// Psych.s08e04.sub.itasa
		Pattern p101 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2})\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, SubtitleRelease.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		MappingMatcher<SimplePropDescriptor> matcher101 = new PatternMappingMatcher<>(p101, grps101.build(), commonPredefMatches);
		episodeMatchers.add(matcher101);

		// Examples:
		// Psych.s08e03.P.sub.itasa
		// Psych.s07e13.720p.sub.itasa
		// Psych.s07e11.P.720p.sub.itasa
		// Psych.s07e11.WEB-DL.sub.itasa
		Pattern p102 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2})\\.(.+?)\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(0, SubtitleRelease.PROP_NAME);
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Season.PROP_NUMBER);
		grps102.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(4, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher102 = new PatternMappingMatcher<>(p102, grps102.build(), commonPredefMatches);
		episodeMatchers.add(matcher102);

		SubtitleReleaseParser episodeSubParser = new SubtitleReleaseParser(new MultiMappingMatcher<>(episodeMatchers.build()), ParsingDefaults.getDefaultSingletonListEpisodeMapper());

		// --------------
		// Multi-Episode Parsers

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> multiEpisodeMatchers = ImmutableList.builder();

		// Examples:
		// Psych.s07e15-16.sub.itasa
		Pattern p201 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2}-\\d{2})\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(0, SubtitleRelease.PROP_NAME);
		grps201.put(1, Series.PROP_NAME);
		grps201.put(2, Season.PROP_NUMBER);
		grps201.put(3, Episode.PROP_NUMBER_IN_SEASON);
		MappingMatcher<SimplePropDescriptor> matcher201 = new PatternMappingMatcher<>(p201, grps201.build(), commonPredefMatches);
		multiEpisodeMatchers.add(matcher201);

		// Examples:
		// Psych.s07e15-16.720p.sub.itasa
		// Psych.s07e15-16.WEB-DL.sub.itasa
		Pattern p202 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2}-\\d{2})\\.(.+?)\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps202 = ImmutableMap.builder();
		grps202.put(0, SubtitleRelease.PROP_NAME);
		grps202.put(1, Series.PROP_NAME);
		grps202.put(2, Season.PROP_NUMBER);
		grps202.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps202.put(4, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher202 = new PatternMappingMatcher<>(p202, grps202.build(), commonPredefMatches);
		multiEpisodeMatchers.add(matcher202);

		SubtitleReleaseParser multiEpisodeSubParser = new SubtitleReleaseParser(new MultiMappingMatcher<>(multiEpisodeMatchers.build()), ParsingDefaults.getDefaultMultiEpisodeMapper());

		TypeBasedParsingService service = new TypeBasedParsingService(SITE.getName());
		service.register(SubtitleRelease.class, episodeSubParser);
		service.register(SubtitleRelease.class, multiEpisodeSubParser);
		return service;
	}

	public static Site getSite()
	{
		return SITE;
	}

	public static final ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static List<ParserEntry<?>> getParserEntries()
	{
		return PARSING_SERVICE.getEntries();
	}
}
