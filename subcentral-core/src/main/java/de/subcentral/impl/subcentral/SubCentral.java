package de.subcentral.impl.subcentral;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.PropParsingService;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.util.SimplePropDescriptor;

public class SubCentral
{
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();
	static
	{
		PARSING_SERVICE.setParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		SubtitleAdjustmentParser epiParser = new SubtitleAdjustmentParser("subcentral.de");

		// Episode matchers
		// The.Last.Ship.S01E06.HDTV.x264-LOL.de-SCuTV4U
		Pattern p101 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)-(\\w+)\\.(de|VO|ger|german)(?:-|\\.)([\\w&]+)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(4, Release.PROP_TAGS);
		grps101.put(5, Release.PROP_GROUP);
		grps101.put(6, Subtitle.PROP_LANGUAGE);
		grps101.put(7, Subtitle.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<>(p101, grps101.build(), ImmutableMap.of(Series.PROP_TYPE,
				Series.TYPE_SEASONED));

		System.out.println(matcher101.match("The.Last.Ship.S01E06.HDTV.x264-LOL.de-SCuTV4U"));

		epiParser.setMatchers(ImmutableList.of(matcher101));
		epiParser.setPps(PropParsingService.DEFAULT);

		return ImmutableListMultimap.of(SubtitleAdjustment.class, epiParser);
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static ListMultimap<Class<?>, Parser<?>> getParsers()
	{
		return PARSING_SERVICE.getParsers();
	}

	private SubCentral()
	{
		// utility class
	}

}
