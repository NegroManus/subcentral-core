package de.subcentral.support.thescene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.Parsings;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.ClassBasedParsingService;
import de.subcentral.core.parsing.SimplePropFromStringService;
import de.subcentral.core.util.SimplePropDescriptor;

public class TheScene
{
	public static final String					DOMAIN			= "scene";
	private static final ClassBasedParsingService	PARSING_SERVICE	= new ClassBasedParsingService(DOMAIN);

	static
	{
		PARSING_SERVICE.registerAllParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		String firstTagPattern = buildFirstTagPattern();

		SimplePropFromStringService pps = new SimplePropFromStringService();
		ImmutableMap.Builder<SimplePropDescriptor, Function<String, ?>> propFromStringFns = ImmutableMap.builder();
		propFromStringFns.put(Episode.PROP_DATE, s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US)));
		pps.setPropFromStringFunctions(propFromStringFns.build());

		// SINGLE EPISODES
		ReleaseParser epiRlsParser = new ReleaseParser(Parsings.getDefaultSingletonListEpisodeMapper());

		// Seasoned episode
		Pattern p101 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)\\.(" + firstTagPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, Release.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(4, Episode.PROP_TITLE);
		grps101.put(5, Release.PROP_TAGS);
		grps101.put(6, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<SimplePropDescriptor>(p101,
				grps101.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		Pattern p102 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(0, Release.PROP_NAME);
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Season.PROP_NUMBER);
		grps102.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(4, Release.PROP_TAGS);
		grps102.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher102 = new MappingMatcher<SimplePropDescriptor>(p102,
				grps102.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		// Alternate naming scheme (used for example by UK group FoV) "The_Fall.2x02.720p_HDTV_x264-FoV"
		Pattern p112 = Pattern.compile("(.*?)\\.(\\d{1,2})x(\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps112 = ImmutableMap.builder();
		grps112.put(0, Release.PROP_NAME);
		grps112.put(1, Series.PROP_NAME);
		grps112.put(2, Season.PROP_NUMBER);
		grps112.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps112.put(4, Release.PROP_TAGS);
		grps112.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher112 = new MappingMatcher<SimplePropDescriptor>(p112,
				grps112.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		// Mini-series episode
		Pattern p201 = Pattern.compile("(.*?)\\.E(\\d{2})\\.(.*?)\\.(" + firstTagPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(0, Release.PROP_NAME);
		grps201.put(1, Series.PROP_NAME);
		grps201.put(2, Episode.PROP_NUMBER_IN_SERIES);
		grps201.put(3, Episode.PROP_TITLE);
		grps201.put(4, Release.PROP_TAGS);
		grps201.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher201 = new MappingMatcher<SimplePropDescriptor>(p201,
				grps201.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_MINI_SERIES));

		Pattern p202 = Pattern.compile("(.*?)\\.E(\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps202 = ImmutableMap.builder();
		grps202.put(0, Release.PROP_NAME);
		grps202.put(1, Series.PROP_NAME);
		grps202.put(2, Episode.PROP_NUMBER_IN_SERIES);
		grps202.put(3, Release.PROP_TAGS);
		grps202.put(4, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher202 = new MappingMatcher<SimplePropDescriptor>(p202,
				grps202.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_MINI_SERIES));

		// Dated episode
		Pattern p301 = Pattern.compile("(.*?)\\.(\\d{4}\\.\\d{2}\\.\\d{2})\\.(.*?)\\.(" + firstTagPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps301 = ImmutableMap.builder();
		grps301.put(0, Release.PROP_NAME);
		grps301.put(1, Series.PROP_NAME);
		grps301.put(2, Episode.PROP_DATE);
		grps301.put(3, Episode.PROP_TITLE);
		grps301.put(4, Release.PROP_TAGS);
		grps301.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher301 = new MappingMatcher<SimplePropDescriptor>(p301,
				grps301.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_DATED));

		Pattern p302 = Pattern.compile("(.*?)\\.(\\d{4}\\.\\d{2}\\.\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps302 = ImmutableMap.builder();
		grps302.put(0, Release.PROP_NAME);
		grps302.put(1, Series.PROP_NAME);
		grps302.put(2, Episode.PROP_DATE);
		grps302.put(3, Release.PROP_TAGS);
		grps302.put(4, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher302 = new MappingMatcher<SimplePropDescriptor>(p302,
				grps302.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_DATED));

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> epiRlsMatchers = ImmutableList.builder();
		epiRlsMatchers.add(matcher101);
		epiRlsMatchers.add(matcher102);
		epiRlsMatchers.add(matcher112);
		epiRlsMatchers.add(matcher201);
		epiRlsMatchers.add(matcher202);
		epiRlsMatchers.add(matcher301);
		epiRlsMatchers.add(matcher302);
		epiRlsParser.setMatchers(epiRlsMatchers.build());
		epiRlsParser.setPropFromStringService(pps);

		// MULTI-EPISODES
		ReleaseParser multiEpiRlsParser = new ReleaseParser(Parsings.getDefaultMultiEpisodeMapper());

		// Multi-episode (seasoned, range)
		Pattern p401 = Pattern.compile("(.*?)\\.S(\\d{2})(E\\d{2}-E\\d{2})\\.(.*?)\\.(" + firstTagPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps401 = ImmutableMap.builder();
		grps401.put(0, Release.PROP_NAME);
		grps401.put(1, Series.PROP_NAME);
		grps401.put(2, Season.PROP_NUMBER);
		grps401.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps401.put(4, Episode.PROP_TITLE);
		grps401.put(5, Release.PROP_TAGS);
		grps401.put(6, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher401 = new MappingMatcher<SimplePropDescriptor>(p401,
				grps401.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		Pattern p402 = Pattern.compile("(.*?)\\.S(\\d{2})(E\\d{2}-E\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps402 = ImmutableMap.builder();
		grps402.put(0, Release.PROP_NAME);
		grps402.put(1, Series.PROP_NAME);
		grps402.put(2, Season.PROP_NUMBER);
		grps402.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps402.put(4, Release.PROP_TAGS);
		grps402.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher402 = new MappingMatcher<SimplePropDescriptor>(p402,
				grps402.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		// Multi-episode (seasoned, addition)
		Pattern p451 = Pattern.compile("(.*?)\\.S(\\d{2})(E\\d{2}(?:\\+?E\\d{2})+)\\.(.*?)\\.(" + firstTagPattern + "\\..*)-(\\w+)",
				Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps451 = ImmutableMap.builder();
		grps451.put(0, Release.PROP_NAME);
		grps451.put(1, Series.PROP_NAME);
		grps451.put(2, Season.PROP_NUMBER);
		grps451.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps451.put(4, Episode.PROP_TITLE);
		grps451.put(5, Release.PROP_TAGS);
		grps451.put(6, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher451 = new MappingMatcher<SimplePropDescriptor>(p451,
				grps451.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		Pattern p452 = Pattern.compile("(.*?)\\.S(\\d{2})(E\\d{2}(?:\\+?E\\d{2})+)\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps452 = ImmutableMap.builder();
		grps452.put(0, Release.PROP_NAME);
		grps452.put(1, Series.PROP_NAME);
		grps452.put(2, Season.PROP_NUMBER);
		grps452.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps452.put(4, Release.PROP_TAGS);
		grps452.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher452 = new MappingMatcher<SimplePropDescriptor>(p452,
				grps452.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> multiEpiRlsMatchers = ImmutableList.builder();
		multiEpiRlsMatchers.add(matcher401);
		multiEpiRlsMatchers.add(matcher402);
		multiEpiRlsMatchers.add(matcher451);
		multiEpiRlsMatchers.add(matcher452);
		multiEpiRlsParser.setMatchers(multiEpiRlsMatchers.build());
		multiEpiRlsParser.setPropFromStringService(pps);

		return ImmutableListMultimap.of(Release.class, epiRlsParser, Release.class, multiEpiRlsParser);
	}

	public static String buildFirstTagPattern()
	{
		StringBuilder knownTag = new StringBuilder();
		knownTag.append("(?:REAL|PROPER|REPACK|DiRFIX|");
		knownTag.append(Joiner.on('|').join(getAllLanguageTags()));
		knownTag.append("|720p|1080p|HDTV|PDTV|WS|HR|WebHD|(?:DVD|WEB|BD|BluRay)(?:-)?(?:Rip)?");
		knownTag.append("|iNTERNAL)");

		StringBuilder tagPattern = new StringBuilder();
		// add a negative look-behind for a tag
		// so that the pattern does not match releases without a title
		tagPattern.append("(?<!");
		tagPattern.append(knownTag);
		// the dot after the tag
		tagPattern.append("\\.)");
		// first recognized tag
		tagPattern.append(knownTag);

		return tagPattern.toString();
	}

	private static Set<String> getAllLanguageTags()
	{
		Locale[] allLocales = Locale.getAvailableLocales();
		Set<String> allLangs = new HashSet<>(allLocales.length / 4);
		allLangs.add("MULTi");
		allLangs.add("FLEMISH");
		for (Locale l : allLocales)
		{
			String displayLang = l.getDisplayLanguage(Locale.ENGLISH);
			if (!StringUtils.isEmpty(displayLang))
			{
				allLangs.add(displayLang);
			}
		}
		return allLangs;
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static ListMultimap<Class<?>, Parser<?>> getAllParsers()
	{
		return PARSING_SERVICE.getParsers();
	}

	private TheScene()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}
