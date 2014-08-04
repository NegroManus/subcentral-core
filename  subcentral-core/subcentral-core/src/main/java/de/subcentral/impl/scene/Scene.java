package de.subcentral.impl.scene;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.PropParsingService;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.util.SimplePropDescriptor;

public class Scene
{
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();

	static
	{
		PARSING_SERVICE.setParsers(ImmutableList.of(initReleaseParser()));
	}

	private static Parser<Release> initReleaseParser()
	{
		ReleaseParser rlsParser = new ReleaseParser("scene");

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		Pattern p101 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(4, Release.PROP_TAGS);
		grps101.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<SimplePropDescriptor>(p101,
				grps101.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		Pattern p102 = Pattern.compile("(.*?)\\.E(\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Episode.PROP_NUMBER_IN_SERIES);
		grps102.put(3, Release.PROP_TAGS);
		grps102.put(4, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher102 = new MappingMatcher<SimplePropDescriptor>(p102,
				grps102.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_MINI_SERIES));

		Pattern p103 = Pattern.compile("(.*?)\\.(\\d{4}\\.\\d{2}\\.\\d{2})\\.(.*?)-(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps103 = ImmutableMap.builder();
		grps103.put(1, Series.PROP_NAME);
		grps103.put(2, Episode.PROP_DATE);
		grps103.put(3, Release.PROP_TAGS);
		grps103.put(4, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher103 = new MappingMatcher<SimplePropDescriptor>(p103,
				grps103.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_DATED));

		matchers.add(matcher101);
		matchers.add(matcher102);
		matchers.add(matcher103);

		rlsParser.setMatchers(matchers.build());

		PropParsingService pps = new PropParsingService();
		ImmutableMap.Builder<Class<?>, Function<String, ?>> typeFromStringFns = ImmutableMap.builder();
		typeFromStringFns.put(LocalDate.class, s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("uuuu.MM.dd", Locale.US)));
		pps.setTypeFromStringFunctions(typeFromStringFns.build());
		rlsParser.setPps(pps);
		return rlsParser;
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	private Scene()
	{

	}

}
