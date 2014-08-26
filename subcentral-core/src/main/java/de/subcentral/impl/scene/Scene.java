package de.subcentral.impl.scene;

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
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.PropParsingService;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.standardizing.SimpleStandardizingService;
import de.subcentral.core.standardizing.Standardizer;
import de.subcentral.core.util.SimplePropDescriptor;

public class Scene
{
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();

	static
	{
		PARSING_SERVICE.setParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		String tagsPattern = buildFirstTagPattern();

		ReleaseParser rlsParser = new ReleaseParser("scene");

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		// Seasoned episode
		Pattern p101 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)\\.(" + tagsPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
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

		// Mini-series episode
		Pattern p201 = Pattern.compile("(.*?)\\.E(\\d{2})\\.(.*?)\\.(" + tagsPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
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
		Pattern p301 = Pattern.compile("(.*?)\\.(\\d{4}\\.\\d{2}\\.\\d{2})\\.(.*?)\\.(" + tagsPattern + "\\..*)-(\\w+)", Pattern.CASE_INSENSITIVE);
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

		matchers.add(matcher101);
		matchers.add(matcher102);
		matchers.add(matcher201);
		matchers.add(matcher202);
		matchers.add(matcher301);
		matchers.add(matcher302);

		rlsParser.setMatchers(matchers.build());

		PropParsingService pps = new PropParsingService();
		ImmutableMap.Builder<SimplePropDescriptor, Function<String, ?>> propFromStringFns = ImmutableMap.builder();
		propFromStringFns.put(Episode.PROP_DATE, s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US)));
		pps.setPropFromStringFunctions(propFromStringFns.build());
		rlsParser.setPropParsingService(pps);

		SimpleStandardizingService ss = new SimpleStandardizingService();
		ImmutableListMultimap.Builder<Class<?>, Standardizer<?>> standardizers = ImmutableListMultimap.builder();
		standardizers.put(Episode.class, (Episode e) -> removeDotsInEpisode(e));
		standardizers.put(Movie.class, (Movie m) -> removeDotsInMovie(m));
		standardizers.put(Release.class, (Release r) -> Releases.standardizeTags(r));
		ss.setStandardizers(standardizers.build());
		rlsParser.setStandardizingService(ss);

		return ImmutableListMultimap.of(Release.class, rlsParser);
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

	public static ListMultimap<Class<?>, Parser<?>> getParsers()
	{
		return PARSING_SERVICE.getParsers();
	}

	public static String removeDots(String text)
	{
		return text == null ? null : text.replace('.', ' ');
	}

	public static Movie removeDotsInMovie(Movie mov)
	{
		if (mov == null)
		{
			return null;
		}
		mov.setName(removeDots(mov.getName()));
		mov.setTitle(removeDots(mov.getTitle()));
		return mov;
	}

	public static Episode removeDotsInEpisode(Episode epi)
	{
		if (epi == null)
		{
			return null;
		}
		epi.setTitle(removeDots(epi.getTitle()));
		if (epi.isPartOfSeason())
		{
			epi.getSeason().setTitle(removeDots(epi.getSeason().getTitle()));
		}
		if (epi.getSeries() != null)
		{
			epi.getSeries().setName(removeDots(epi.getSeries().getName()));
			epi.getSeries().setTitle(removeDots(epi.getSeries().getTitle()));
		}
		return epi;
	}

	private Scene()
	{

	}
}
