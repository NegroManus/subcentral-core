package de.subcentral.impl.addic7ed;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.MappingServiceImpl;
import de.subcentral.core.parsing.NumericGroupMappingMatcher;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ParsingServiceImpl;
import de.subcentral.core.parsing.SubtitleAdjustmentMapper;
import de.subcentral.core.util.SimplePropDescriptor;

public class Addic7ed
{
	private static final ParsingServiceImpl	PARSING_SERVICE	= new ParsingServiceImpl();

	static
	{
		MappingServiceImpl ms = new MappingServiceImpl();
		ImmutableMap<Class<?>, Mapper<?>> mappers = ImmutableMap.of(SubtitleAdjustment.class, new SubtitleAdjustmentMapper());
		ms.setMappers(mappers);

		PARSING_SERVICE.setMappingService(ms);
		PARSING_SERVICE.setMatchers(initMatchers());
	}

	private static ListMultimap<Class<?>, MappingMatcher> initMatchers()
	{
		// final String tagsPattern = "((HI|C|orig|updated)+)";
		final String langPattern = "(Albanian|Arabic|Armenian|Azerbaijani|Bengali|Bosnian|Bulgarian|Català|Chinese \\(Simplified\\)|Chinese \\(Traditional\\)|Croatian|Czech|Danish|Dutch|English|Euskera|Finnish|French|Galego|German|Greek|Hebrew|Hungarian|Indonesian|Italian|Japanese|Korean|Macedonian|Malay|Norwegian|Persian|Polish|Portuguese|Portuguese \\(Brazilian \\)|Romanian|Russian|Serbian \\(Cyrillic\\)|Serbian \\(Latin\\)|Slovak|Slovenian|Spanish|Spanish \\(Latin America\\)|Spanish \\(Spain\\)|Swedish|Thai|Turkish|Ukrainian|Vietnamese)";

		ImmutableListMultimap.Builder<Class<?>, MappingMatcher> map = ImmutableListMultimap.builder();

		// init the matchers
		// Episode matchers

		// WEB-DL but then no "-" after that (which would indicate a group)
		// Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com
		Pattern p101 = Pattern.compile("(.*?) - (\\d{2})x(\\d{2}) - (.+?)\\.(WEB-DL\\.[\\w\\.]+)\\." + langPattern + "\\.(.+)\\.(Addic7ed\\.com)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(4, Episode.PROP_TITLE);
		grps101.put(5, Release.PROP_TAGS);
		grps101.put(6, Subtitle.PROP_LANGUAGE);
		grps101.put(7, Subtitle.PROP_TAGS);
		grps101.put(8, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher101 = new NumericGroupMappingMatcher(p101, grps101.build());

		// Expecting no dots in the episode title, then a dot, then "WEB-DL" or a group
		// Examples:
		// "Psych - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com"
		Pattern p102 = Pattern.compile("((.*?)(\\s+\\(((\\d{4})|(\\p{Upper}{2}))\\))?) - (\\d{2})x(\\d{2}) - ([^\\.]+?)\\.((WEB-DL)|(\\w+))\\."
				+ langPattern + "\\.(.+)\\.(Addic7ed\\.com)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Series.PROP_TITLE);
		// 3 is the optional group
		// 4 is (...|...)
		grps102.put(5, Series.PROP_DATE); // e.g. "2004"
		grps102.put(6, Series.PROP_COUNTRIES_OF_ORIGIN); // e.g. "UK"
		grps102.put(7, Season.PROP_NUMBER);
		grps102.put(8, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(9, Episode.PROP_TITLE);
		// 10 is (...|...)
		grps102.put(11, Release.PROP_TAGS); // WEB-DL
		grps102.put(12, Release.PROP_GROUP);
		grps102.put(13, Subtitle.PROP_LANGUAGE);
		grps102.put(14, Subtitle.PROP_TAGS);
		grps102.put(15, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher102 = new NumericGroupMappingMatcher(p102, grps102.build());

		// Expecting no dots in the episode title, then a dot, then release tags, then a non-wordchar delimiter, then "WEB-DL" or a group.
		// Examples:
		// "10 Things I Hate About You - 01x01 - Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com"
		// "Switched At Birth - 03x04 - It Hurts to Wait With Love If Love Is Somewhere Else.HDTV.x264-EXCELLENCE.Dutch.orig.Addic7ed.com"
		// "Vikings - 01x08 - Sacrifice.x264.2HD.English.C.orig.Addic7ed.com"
		// "Out There (2013) - 01x09 - Viking Days.480p.WEB-DL.x264-mSD.English.C.orig.Addic7ed.com"
		// "Psych - 01x01 - Pilot.DVDRip TOPAZ.French.orig.Addic7ed.com"
		Pattern p103 = Pattern.compile("(.*?) - (\\d{2})x(\\d{2}) - ([^\\.]+?)\\.([\\w+\\.-]+?)\\W((WEB-DL)|(\\w+))\\." + langPattern
				+ "\\.(.+)\\.(Addic7ed\\.com)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps103 = ImmutableMap.builder();
		grps103.put(1, Series.PROP_NAME);
		grps103.put(2, Season.PROP_NUMBER);
		grps103.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps103.put(4, Episode.PROP_TITLE);
		grps103.put(5, Release.PROP_TAGS);
		// 6 is (..|..)
		grps103.put(7, Release.PROP_TAGS);
		grps103.put(8, Release.PROP_GROUP);
		grps103.put(9, Subtitle.PROP_LANGUAGE);
		grps103.put(10, Subtitle.PROP_TAGS);
		grps103.put(11, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher103 = new NumericGroupMappingMatcher(p103, grps103.build());

		// Episode title may contain dots, then a dot, then release tags, then a non-wordchar delimiter, then "WEB-DL" or a group
		// Examples:
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264.DIMENSION.English.HI.Addic7ed.com
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com
		Pattern p104 = Pattern.compile("(.*?) - (\\d{2})x(\\d{2}) - (.+?)\\.([\\w+\\.-]+?)\\W((WEB-DL)|(\\w+))\\." + langPattern
				+ "\\.(.+)\\.(Addic7ed\\.com)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps104 = ImmutableMap.builder();
		grps104.put(1, Series.PROP_NAME);
		grps104.put(2, Season.PROP_NUMBER);
		grps104.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps104.put(4, Episode.PROP_TITLE);
		grps104.put(5, Release.PROP_TAGS);
		// 6 is (..|..)
		grps104.put(7, Release.PROP_TAGS);
		grps104.put(8, Release.PROP_GROUP);
		grps104.put(9, Subtitle.PROP_LANGUAGE);
		grps104.put(10, Subtitle.PROP_TAGS);
		grps104.put(11, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher104 = new NumericGroupMappingMatcher(p104, grps104.build());

		// Episode title may contain dots, then a dot, then "WEB-DL" or a group
		// Examples:
		// "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com"
		// "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com"
		Pattern p105 = Pattern.compile("(.*?) - (\\d{2})x(\\d{2}) - (.+?)\\.((WEB-DL)|(\\w+))\\." + langPattern + "\\.(.+)\\.(Addic7ed\\.com)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps105 = ImmutableMap.builder();
		grps105.put(1, Series.PROP_NAME);
		grps105.put(2, Season.PROP_NUMBER);
		grps105.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps105.put(4, Episode.PROP_TITLE);
		// 5 is (...|...)
		grps105.put(6, Release.PROP_TAGS);
		grps105.put(7, Release.PROP_GROUP);
		grps105.put(8, Subtitle.PROP_LANGUAGE);
		grps105.put(9, Subtitle.PROP_TAGS);
		grps105.put(10, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher105 = new NumericGroupMappingMatcher(p105, grps105.build());

		// FOR TESTING
		// matcher102.match("Psych (UK) - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com").forEach((k, v) -> System.out.println(k
		// + " = " + v));

		// --------------
		// Movie matchers
		// "Winter's Tale (2014).DVD-Rip.English.orig.Addic7ed.com"
		Pattern p201 = Pattern.compile("((.*?) \\((\\d{4})\\))\\.([\\w-]+)\\." + langPattern + "\\.(.+)\\.(Addic7ed\\.com)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(1, Movie.PROP_NAME);
		grps201.put(2, Movie.PROP_TITLE);
		grps201.put(3, Movie.PROP_DATE);
		grps201.put(4, Release.PROP_TAGS);
		grps201.put(5, Subtitle.PROP_LANGUAGE);
		grps201.put(6, Subtitle.PROP_TAGS);
		grps201.put(7, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher201 = new NumericGroupMappingMatcher(p201, grps201.build());

		// add all the matchers to the map
		map.put(SubtitleAdjustment.class, matcher101);
		map.put(SubtitleAdjustment.class, matcher102);
		map.put(SubtitleAdjustment.class, matcher103);
		map.put(SubtitleAdjustment.class, matcher104);
		map.put(SubtitleAdjustment.class, matcher105);
		map.put(SubtitleAdjustment.class, matcher201);

		return map.build();
	}

	public static final ParsingService getAddi7edParsingService()
	{
		return PARSING_SERVICE;
	}

	public Addic7ed()
	{
		// utility class
	}

}
