package de.subcentral.support.addic7edcom;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.CompoundMappingMatcher;
import de.subcentral.core.parse.CompoundMappingMatcher.GroupEntry;
import de.subcentral.core.parse.MappingMatcher;
import de.subcentral.core.parse.MultiMappingMatcher;
import de.subcentral.core.parse.Parser;
import de.subcentral.core.parse.ParsingDefaults;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.parse.PatternMappingMatcher;
import de.subcentral.core.parse.SubtitleReleaseParser;
import de.subcentral.core.parse.TypeBasedParsingService;
import de.subcentral.core.parse.TypeBasedParsingService.ParserEntry;
import de.subcentral.core.util.SimplePropDescriptor;

public class Addic7edCom
{
	private static final Site						SITE						= new Site("addic7ed.com", "Addic7ed.com", "http://www.addic7ed.com/");
	private static final TypeBasedParsingService	PARSING_SERVICE				= initParsingService();
	private static final String						LANG_PATTERN				= "(Albanian|Arabic|Armenian|Azerbaijani|Bengali|Bosnian|Bulgarian|Català|Chinese \\(Simplified\\)|Chinese \\(Traditional\\)|Croatian|Czech|Danish|Dutch|English|Euskera|Finnish|French|Galego|German|Greek|Hebrew|Hungarian|Indonesian|Italian|Japanese|Korean|Macedonian|Malay|Norwegian|Persian|Polish|Portuguese|Portuguese \\(Brazilian\\)|Romanian|Russian|Serbian \\(Cyrillic\\)|Serbian \\(Latin\\)|Slovak|Slovenian|Spanish|Spanish \\(Latin America\\)|Spanish \\(Spain\\)|Swedish|Thai|Turkish|Ukrainian|Vietnamese)";
	private static final String						TAGS_PATTERN				= "(.+)";
	private static final String						LANG_TAGS_SOURCE_PATTERN	= "\\." + LANG_PATTERN + "\\." + TAGS_PATTERN + "\\.Addic7ed\\.com";
	private static final String						RLS_TAGS_PATTERN			= "(?:(?:DVD|WEB)[.-]?(?:DL|Rip)|INTERNAL)";

	private Addic7edCom()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}

	private static TypeBasedParsingService initParsingService()
	{
		TypeBasedParsingService service = new TypeBasedParsingService(SITE.getName());
		service.register(SubtitleRelease.class, createEpisodeSubtitleReleaseParser());
		service.register(SubtitleRelease.class, createMovieSubtitleReleaseParser());
		return service;
	}

	private static Parser<SubtitleRelease> createEpisodeSubtitleReleaseParser()
	{
		// Series - 01x01 - (.*).English.C.orig.Addic7ed.com
		Pattern pattern = Pattern.compile(ParsingDefaults.PATTERN_MEDIA_NAME + " - (\\d{2})x(\\d{2}) - (.*)" + LANG_TAGS_SOURCE_PATTERN);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> groups = ImmutableMap.builder();
		groups.put(0, GroupEntry.ofKey(SubtitleRelease.PROP_NAME));
		groups.put(1, GroupEntry.ofKey(Series.PROP_NAME));
		groups.put(2, GroupEntry.ofKey(Series.PROP_TITLE));
		groups.put(3, GroupEntry.ofKey(Series.PROP_DATE)); // e.g. "2004"
		groups.put(4, GroupEntry.ofKey(Series.PROP_COUNTRIES)); // e.g. "UK"
		groups.put(5, GroupEntry.ofKey(Season.PROP_NUMBER));
		groups.put(6, GroupEntry.ofKey(Episode.PROP_NUMBER_IN_SEASON));
		groups.put(7, GroupEntry.ofMatcher(createEpisodeTitleAndReleaseMatcher()));
		groups.put(8, GroupEntry.ofKey(Subtitle.PROP_LANGUAGE));
		groups.put(9, GroupEntry.ofKey(SubtitleRelease.PROP_TAGS));
		ImmutableMap.Builder<SimplePropDescriptor, String> predefMatches = ImmutableMap.builder();
		predefMatches.put(Subtitle.PROP_SOURCE, getSite().getName());
		predefMatches.put(Series.PROP_TYPE, Series.TYPE_SEASONED);
		MappingMatcher<SimplePropDescriptor> matcher = new CompoundMappingMatcher<>(pattern, groups.build(), predefMatches.build());

		return new SubtitleReleaseParser(matcher, ParsingDefaults.getDefaultSingletonListEpisodeMapper());
	}

	private static MappingMatcher<SimplePropDescriptor> createEpisodeTitleAndReleaseMatcher()
	{
		String epiTitleAndDot = "(.+?)\\.";

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		// (WEB-DL|Rip), then a non-word-char, then only one "tag" -> the group in most of the cases
		// Examples:
		// From Dusk Till Dawn_ The Series - 01x01 - Pilot.Webrip.2HD.English.C.orig.Addic7ed.com
		Pattern p100 = Pattern.compile(epiTitleAndDot + "(" + RLS_TAGS_PATTERN + ")\\W([\\w]+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps100 = ImmutableMap.builder();
		grps100.put(1, Episode.PROP_TITLE);
		grps100.put(2, Release.PROP_TAGS);
		grps100.put(3, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher100 = new PatternMappingMatcher<>(p100, grps100.build());
		matchers.add(matcher100);

		// WEB-DL|Rip but then no "-" after that (which would indicate a group)
		// Examples:
		// Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com
		Pattern p101 = Pattern.compile(epiTitleAndDot + "(" + RLS_TAGS_PATTERN + "\\.[\\w\\.]+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(1, Episode.PROP_TITLE);
		grps101.put(2, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher101 = new PatternMappingMatcher<>(p101, grps101.build());
		matchers.add(matcher101);

		// Episode title ends with a dot, then known release tags or a group
		// Examples:
		// "CSI_ Cyber - 02x05 - hack E.R..DIMENSION.English.C.orig.Addic7ed.com"
		Pattern p103 = Pattern.compile("(.+?\\.)\\.(?:(" + RLS_TAGS_PATTERN + ")|(\\w+))", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps103 = ImmutableMap.builder();
		grps103.put(1, Episode.PROP_TITLE);
		grps103.put(2, Release.PROP_TAGS);
		grps103.put(3, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher103 = new PatternMappingMatcher<>(p103, grps103.build());
		matchers.add(matcher103);

		// Episode title may contain dots, then a dot, then
		// a) either "WEB-DL" or
		// b) release tags, then a non-wordchar delimiter, then
		// b1) either "WEB-DL" or
		// b2) a group
		// Examples:
		// a) Hannibal - 03x10 - ...And the Woman Clothed in Sun.WEB-DL.English.HI.C.orig.Addic7ed.com
		// b1) 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com
		// b2) 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com
		Pattern p104 = Pattern.compile("(.+?)\\.(?:(" + RLS_TAGS_PATTERN + ")|([\\w+\\.-]+?)\\W(?:(" + RLS_TAGS_PATTERN + ")|(\\w+)))", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps104 = ImmutableMap.builder();
		grps104.put(1, Episode.PROP_TITLE);
		grps104.put(2, Release.PROP_TAGS);
		grps104.put(3, Release.PROP_TAGS);
		grps104.put(4, Release.PROP_TAGS);
		grps104.put(5, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher104 = new PatternMappingMatcher<>(p104, grps104.build());
		matchers.add(matcher104);

		// Episode title may contain dots, then a dot, then "WEB-DL|Rip" or a group
		// Examples:
		// "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com"
		// "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com"
		Pattern p105 = Pattern.compile("(.+?)\\.(?:(" + RLS_TAGS_PATTERN + ")|(\\w+))", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps105 = ImmutableMap.builder();
		grps105.put(1, Episode.PROP_TITLE);
		grps105.put(2, Release.PROP_TAGS);
		grps105.put(3, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher105 = new PatternMappingMatcher<>(p105, grps105.build());
		matchers.add(matcher105);

		// Multiple release groups (and hence matching releases) separated by comma
		// Examples
		// "Finding Carter - 01x07 - Throw Momma From the Train.KILLERS, MSD.English.C.orig.Addic7ed.com"
		Pattern p106 = Pattern.compile("(.+?)\\.(\\w+(?:,\\s+\\w+)*)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps106 = ImmutableMap.builder();
		grps106.put(1, Episode.PROP_TITLE);
		grps106.put(2, Release.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher106 = new PatternMappingMatcher<>(p106, grps106.build());
		matchers.add(matcher106);

		// Release group, then tags in parenthesis
		// Death in Paradise - 04x04 - Series 4, Episode 4.FoV (HDTV + 720p).English.C.orig.Addic7ed.com
		// The Saboteurs (aka The Heavy Water War) - 01x06 - Episode 6 (Finale).TVC (HDTV).English.C.orig.Addic7ed.com
		Pattern p107 = Pattern.compile("(.+?)\\.(\\w+)\\s+\\((.*?)\\)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps107 = ImmutableMap.builder();
		grps107.put(1, Episode.PROP_TITLE);
		grps107.put(2, Release.PROP_GROUP);
		grps107.put(3, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher107 = new PatternMappingMatcher<>(p107, grps107.build());
		matchers.add(matcher107);

		// TODO: Actually "(HDTV + 720p)" are 2 releases "HDTV" and "720p.HDTV". Two releases should be matched.
		// But thats not possible with current matching algorithm. It has to be possible to numerate the objects if multiple

		return new MultiMappingMatcher<>(matchers.build());
	}

	private static Parser<SubtitleRelease> createMovieSubtitleReleaseParser()
	{
		// Movie (YEAR) - 01x01 - (.*).English.C.orig.Addic7ed.com
		Pattern pattern = Pattern.compile("(.*?)\\s+\\((\\d{4})\\)\\.(.*)" + LANG_TAGS_SOURCE_PATTERN);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> groups = ImmutableMap.builder();
		groups.put(0, GroupEntry.ofKey(SubtitleRelease.PROP_NAME));
		groups.put(1, GroupEntry.ofKey(Movie.PROP_NAME));
		groups.put(2, GroupEntry.ofKey(Movie.PROP_DATE));
		groups.put(3, GroupEntry.ofMatcher(createReleaseMatcher()));
		groups.put(4, GroupEntry.ofKey(Subtitle.PROP_LANGUAGE));
		groups.put(5, GroupEntry.ofKey(SubtitleRelease.PROP_TAGS));
		ImmutableMap.Builder<SimplePropDescriptor, String> predefMatches = ImmutableMap.builder();
		predefMatches.put(Subtitle.PROP_SOURCE, getSite().getName());
		MappingMatcher<SimplePropDescriptor> matcher = new CompoundMappingMatcher<>(pattern, groups.build(), predefMatches.build());

		return new SubtitleReleaseParser(matcher, ParsingDefaults.getDefaultSingletonListMovieMapper());
	}

	private static MappingMatcher<SimplePropDescriptor> createReleaseMatcher()
	{
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		// "Winter's Tale (2014).DVD-Rip.English.orig.Addic7ed.com"
		Pattern p101 = Pattern.compile("(" + RLS_TAGS_PATTERN + ")", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(1, Release.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher101 = new PatternMappingMatcher<>(p101, grps101.build());
		matchers.add(matcher101);

		// "The Man Behind the Throne (2013).CBFM.English.C.orig.Addic7ed.com"
		Pattern p202 = Pattern.compile("(\\w+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps202 = ImmutableMap.builder();
		grps202.put(1, Release.PROP_GROUP);
		PatternMappingMatcher<SimplePropDescriptor> matcher202 = new PatternMappingMatcher<>(p202, grps202.build());
		matchers.add(matcher202);

		return new MultiMappingMatcher<>(matchers.build());
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
