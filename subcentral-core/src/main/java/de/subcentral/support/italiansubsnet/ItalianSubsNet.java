package de.subcentral.support.italiansubsnet;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.RegularAvMedia;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingDefaults;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.util.SimplePropDescriptor;

public class ItalianSubsNet
{
	public static final String					DOMAIN			= "italiansubs.net";

	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService(DOMAIN);
	static
	{
		PARSING_SERVICE.registerAllParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		ImmutableMap.Builder<SimplePropDescriptor, String> commonPredefMatchesBuilder = ImmutableMap.builder();
		commonPredefMatchesBuilder.put(Subtitle.PROP_SOURCE, DOMAIN);
		commonPredefMatchesBuilder.put(RegularAvMedia.PROP_MEDIA_CONTENT_TYPE, Media.MEDIA_CONTENT_TYPE_VIDEO);
		commonPredefMatchesBuilder.put(Subtitle.PROP_LANGUAGE, "italian");
		ImmutableMap<SimplePropDescriptor, String> commonPredefMatches = commonPredefMatchesBuilder.build();

		// --------------
		// Episode Parsers
		SubtitleAdjustmentParser episodeSubParser = new SubtitleAdjustmentParser(ParsingDefaults.getDefaultSingletonListEpisodeMapper());

		// Examples:
		// Psych.s08e04.sub.itasa
		Pattern p101 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2})\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, SubtitleAdjustment.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<>(p101, grps101.build(), commonPredefMatches);

		// Examples:
		// Psych.s08e03.P.sub.itasa
		// Psych.s07e13.720p.sub.itasa
		// Psych.s07e11.P.720p.sub.itasa
		// Psych.s07e11.WEB-DL.sub.itasa
		Pattern p102 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2})\\.(.+?)\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(0, SubtitleAdjustment.PROP_NAME);
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Season.PROP_NUMBER);
		grps102.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(4, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher102 = new MappingMatcher<>(p102, grps102.build(), commonPredefMatches);

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> episodeMatchers = ImmutableList.builder();
		episodeMatchers.add(matcher101);
		episodeMatchers.add(matcher102);
		episodeSubParser.setMatchers(episodeMatchers.build());

		// --------------
		// Multi-Episode Parsers
		SubtitleAdjustmentParser multiEpisodeSubParser = new SubtitleAdjustmentParser(ParsingDefaults.getDefaultMultiEpisodeMapper());

		// Examples:
		// Psych.s07e15-16.sub.itasa
		Pattern p201 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2}-\\d{2})\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(0, SubtitleAdjustment.PROP_NAME);
		grps201.put(1, Series.PROP_NAME);
		grps201.put(2, Season.PROP_NUMBER);
		grps201.put(3, Episode.PROP_NUMBER_IN_SEASON);
		MappingMatcher<SimplePropDescriptor> matcher201 = new MappingMatcher<>(p201, grps201.build(), commonPredefMatches);

		// Examples:
		// Psych.s07e15-16.720p.sub.itasa
		// Psych.s07e15-16.WEB-DL.sub.itasa
		Pattern p202 = Pattern.compile("(.*)\\.s(\\d{2})e(\\d{2}-\\d{2})\\.(.+?)\\.sub\\.itasa", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps202 = ImmutableMap.builder();
		grps202.put(0, SubtitleAdjustment.PROP_NAME);
		grps202.put(1, Series.PROP_NAME);
		grps202.put(2, Season.PROP_NUMBER);
		grps202.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps202.put(4, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher202 = new MappingMatcher<>(p202, grps202.build(), commonPredefMatches);

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> multiEpisodeMatchers = ImmutableList.builder();

		multiEpisodeMatchers.add(matcher201);
		multiEpisodeMatchers.add(matcher202);
		multiEpisodeSubParser.setMatchers(multiEpisodeMatchers.build());

		return ImmutableListMultimap.of(SubtitleAdjustment.class, episodeSubParser, SubtitleAdjustment.class, multiEpisodeSubParser);
	}

	public static final ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static ListMultimap<Class<?>, Parser<?>> getAllParsers()
	{
		return PARSING_SERVICE.getParsers();
	}

	private ItalianSubsNet()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}