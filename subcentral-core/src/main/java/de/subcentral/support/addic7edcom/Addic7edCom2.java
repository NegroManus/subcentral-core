package de.subcentral.support.addic7edcom;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.DelegatingMappingMatcher;
import de.subcentral.core.parse.DelegatingMappingMatcher.GroupEntry;
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

public class Addic7edCom2
{
	public static final String						SITE_ID			= "addic7ed.com";

	private static final TypeBasedParsingService	PARSING_SERVICE	= new TypeBasedParsingService(SITE_ID);

	static
	{
		PARSING_SERVICE.registerAll(SubtitleRelease.class, initParsers());
	}

	private static Parser<SubtitleRelease> createEpisodeSubtitleReleaseParser()
	{
		// Series - 01x01 - (.*).English.C.orig.Addic7ed.com
		Pattern pattern = Pattern.compile(ParsingDefaults.PATTERN_MEDIA_NAME + " - (\\d{2})x(\\d{2}) - (.*)\\.([\\p{Alpha}() ])\\.((?:(?:HI|C|orig|updated)\\.)+)Addic7ed.com");
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> groups = ImmutableMap.builder();
		groups.put(0, GroupEntry.ofKey(SubtitleRelease.PROP_NAME));
		groups.put(1, GroupEntry.ofKey(Series.PROP_NAME));
		groups.put(2, GroupEntry.ofKey(Series.PROP_TITLE));
		groups.put(3, GroupEntry.ofKey(Series.PROP_DATE)); // e.g. "2004"
		groups.put(4, GroupEntry.ofKey(Series.PROP_COUNTRIES)); // e.g. "UK"
		groups.put(5, GroupEntry.ofKey(Season.PROP_NUMBER));
		groups.put(6, GroupEntry.ofKey(Episode.PROP_NUMBER_IN_SEASON));
		groups.put(7, GroupEntry.ofMatcher(createEpisodeTitleAndVersionMatcher()));
		groups.put(8, GroupEntry.ofKey(Subtitle.PROP_LANGUAGE));
		groups.put(9, GroupEntry.ofKey(SubtitleRelease.PROP_TAGS));
		ImmutableMap<SimplePropDescriptor, String> predefMatches = ImmutableMap.of(Subtitle.PROP_SOURCE, "Addic7ed.com");
		MappingMatcher<SimplePropDescriptor> matcher = new DelegatingMappingMatcher<>(pattern, groups.build(), predefMatches);

		return new SubtitleReleaseParser(matcher, ParsingDefaults.getDefaultSingletonListEpisodeMapper());
	}

	private static MappingMatcher<SimplePropDescriptor> createEpisodeTitleAndVersionMatcher()
	{
		String rlsTagsPttrn = "(?:(?:DVD|WEB)[.-]?(?:DL|Rip)|INTERNAL)";

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		// (WEB-DL|Rip), then a non-word-char, then only one "tag" -> the group in most of the cases
		// Examples:
		// From Dusk Till Dawn_ The Series - 01x01 - Pilot.Webrip.2HD.English.C.orig.Addic7ed.com
		Pattern p100 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(" + rlsTagsPttrn + ")\\W([\\w]+)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps100 = ImmutableMap.builder();
		grps100.put(0, SubtitleRelease.PROP_NAME);
		grps100.put(1, Series.PROP_NAME);
		grps100.put(2, Series.PROP_TITLE);
		grps100.put(3, Series.PROP_DATE); // e.g. "2004"
		grps100.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps100.put(5, Season.PROP_NUMBER);
		grps100.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps100.put(7, Episode.PROP_TITLE);
		grps100.put(8, Release.PROP_TAGS);
		grps100.put(9, Release.PROP_GROUP);
		grps100.put(10, Subtitle.PROP_LANGUAGE);
		grps100.put(11, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher100 = new PatternMappingMatcher<>(p100, grps100.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher100);

		return new MultiMappingMatcher<>(matchers.build());
	}

	private static List<Parser<SubtitleRelease>> initParsers()
	{
		// Common Objects
		String srSsnEpiNumsPttrn = ParsingDefaults.PATTERN_MEDIA_NAME + " - (\\d{2})x(\\d{2}) - ";
		// String tagsPattern = "((HI|C|orig|updated)+)";
		String langPttrn = "(Albanian|Arabic|Armenian|Azerbaijani|Bengali|Bosnian|Bulgarian|Català|Chinese \\(Simplified\\)|Chinese \\(Traditional\\)|Croatian|Czech|Danish|Dutch|English|Euskera|Finnish|French|Galego|German|Greek|Hebrew|Hungarian|Indonesian|Italian|Japanese|Korean|Macedonian|Malay|Norwegian|Persian|Polish|Portuguese|Portuguese \\(Brazilian\\)|Romanian|Russian|Serbian \\(Cyrillic\\)|Serbian \\(Latin\\)|Slovak|Slovenian|Spanish|Spanish \\(Latin America\\)|Spanish \\(Spain\\)|Swedish|Thai|Turkish|Ukrainian|Vietnamese)";
		String langSubTagsSrcPttrn = langPttrn + "\\.(.+)\\.Addic7ed\\.com";
		String rlsTagsPttrn = "(?:(?:DVD|WEB)[.-]?(?:DL|Rip)|INTERNAL)";

		ImmutableMap<SimplePropDescriptor, String> commonPredefMatches = ImmutableMap.of(Subtitle.PROP_SOURCE, "Addic7ed.com");

		// --------------
		// Episode Parser
		// Predefined matches for episodes
		ImmutableMap.Builder<SimplePropDescriptor, String> predefEpisodeMatchesBuilder = ImmutableMap.builder();
		predefEpisodeMatchesBuilder.putAll(commonPredefMatches);
		predefEpisodeMatchesBuilder.put(Series.PROP_TYPE, Series.TYPE_SEASONED);
		ImmutableMap<SimplePropDescriptor, String> predefEpisodeMatches = predefEpisodeMatchesBuilder.build();

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> episodeMatchers = ImmutableList.builder();

		// (WEB-DL|Rip), then a non-word-char, then only one "tag" -> the group in most of the cases
		// Examples:
		// From Dusk Till Dawn_ The Series - 01x01 - Pilot.Webrip.2HD.English.C.orig.Addic7ed.com
		Pattern p100 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(" + rlsTagsPttrn + ")\\W([\\w]+)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps100 = ImmutableMap.builder();
		grps100.put(0, SubtitleRelease.PROP_NAME);
		grps100.put(1, Series.PROP_NAME);
		grps100.put(2, Series.PROP_TITLE);
		grps100.put(3, Series.PROP_DATE); // e.g. "2004"
		grps100.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps100.put(5, Season.PROP_NUMBER);
		grps100.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps100.put(7, Episode.PROP_TITLE);
		grps100.put(8, Release.PROP_TAGS);
		grps100.put(9, Release.PROP_GROUP);
		grps100.put(10, Subtitle.PROP_LANGUAGE);
		grps100.put(11, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher100 = new PatternMappingMatcher<>(p100, grps100.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher100);

		// WEB-DL|Rip but then no "-" after that (which would indicate a group)
		// Examples:
		// Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com
		Pattern p101 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(" + rlsTagsPttrn + "\\.[\\w\\.]+)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, SubtitleRelease.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Series.PROP_TITLE);
		grps101.put(3, Series.PROP_DATE); // e.g. "2004"
		grps101.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps101.put(5, Season.PROP_NUMBER);
		grps101.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(7, Episode.PROP_TITLE);
		grps101.put(8, Release.PROP_TAGS);
		grps101.put(9, Subtitle.PROP_LANGUAGE);
		grps101.put(10, SubtitleRelease.PROP_TAGS);
		PatternMappingMatcher<SimplePropDescriptor> matcher101 = new PatternMappingMatcher<>(p101, grps101.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher101);

		// Episode title ends with a dot, then known release tags or a group
		// Examples:
		// "CSI_ Cyber - 02x05 - hack E.R..DIMENSION.English.C.orig.Addic7ed.com"
		Pattern p103 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?\\.)\\.(?:(" + rlsTagsPttrn + ")|(\\w+))\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps103 = ImmutableMap.builder();
		grps103.put(0, SubtitleRelease.PROP_NAME);
		grps103.put(1, Series.PROP_NAME);
		grps103.put(2, Series.PROP_TITLE);
		grps103.put(3, Series.PROP_DATE); // e.g. "2004"
		grps103.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps103.put(5, Season.PROP_NUMBER);
		grps103.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps103.put(7, Episode.PROP_TITLE);
		grps103.put(8, Release.PROP_TAGS);
		grps103.put(9, Release.PROP_GROUP);
		grps103.put(10, Subtitle.PROP_LANGUAGE);
		grps103.put(11, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher103 = new PatternMappingMatcher<>(p103, grps103.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher103);

		// Episode title may contain dots, then a dot, then
		// a) either "WEB-DL" or
		// b) release tags, then a non-wordchar delimiter, then
		// b1) either "WEB-DL" or
		// b2) a group
		// Examples:
		// a) Hannibal - 03x10 - ...And the Woman Clothed in Sun.WEB-DL.English.HI.C.orig.Addic7ed.com
		// b1) 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com
		// b2) 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com
		Pattern p104 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(?:(" + rlsTagsPttrn + ")|([\\w+\\.-]+?)\\W(?:(" + rlsTagsPttrn + ")|(\\w+)))\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps104 = ImmutableMap.builder();
		grps104.put(0, SubtitleRelease.PROP_NAME);
		grps104.put(1, Series.PROP_NAME);
		grps104.put(2, Series.PROP_TITLE);
		grps104.put(3, Series.PROP_DATE); // e.g. "2004"
		grps104.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps104.put(5, Season.PROP_NUMBER);
		grps104.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps104.put(7, Episode.PROP_TITLE);
		grps104.put(8, Release.PROP_TAGS);
		grps104.put(9, Release.PROP_TAGS);
		grps104.put(10, Release.PROP_TAGS);
		grps104.put(11, Release.PROP_GROUP);
		grps104.put(12, Subtitle.PROP_LANGUAGE);
		grps104.put(13, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher104 = new PatternMappingMatcher<>(p104, grps104.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher104);

		// Episode title may contain dots, then a dot, then "WEB-DL|Rip" or a group
		// Examples:
		// "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com"
		// "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com"
		Pattern p105 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(?:(" + rlsTagsPttrn + ")|(\\w+))\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps105 = ImmutableMap.builder();
		grps105.put(0, SubtitleRelease.PROP_NAME);
		grps105.put(1, Series.PROP_NAME);
		grps105.put(2, Series.PROP_TITLE);
		grps105.put(3, Series.PROP_DATE); // e.g. "2004"
		grps105.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps105.put(5, Season.PROP_NUMBER);
		grps105.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps105.put(7, Episode.PROP_TITLE);
		grps105.put(8, Release.PROP_TAGS);
		grps105.put(9, Release.PROP_GROUP);
		grps105.put(10, Subtitle.PROP_LANGUAGE);
		grps105.put(11, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher105 = new PatternMappingMatcher<>(p105, grps105.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher105);

		// Multiple release groups (and hence matching releases) separated by comma
		// Examples
		// "Finding Carter - 01x07 - Throw Momma From the Train.KILLERS, MSD.English.C.orig.Addic7ed.com"
		Pattern p106 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(\\w+(?:,\\s+\\w+)*)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps106 = ImmutableMap.builder();
		grps106.put(0, SubtitleRelease.PROP_NAME);
		grps106.put(1, Series.PROP_NAME);
		grps106.put(2, Series.PROP_TITLE);
		grps106.put(3, Series.PROP_DATE); // e.g. "2004"
		grps106.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps106.put(5, Season.PROP_NUMBER);
		grps106.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps106.put(7, Episode.PROP_TITLE);
		grps106.put(8, Release.PROP_GROUP);
		grps106.put(9, Subtitle.PROP_LANGUAGE);
		grps106.put(10, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher106 = new PatternMappingMatcher<>(p106, grps106.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher106);

		// Release group, then tags in parenthesis
		// Death in Paradise - 04x04 - Series 4, Episode 4.FoV (HDTV + 720p).English.C.orig.Addic7ed.com
		// The Saboteurs (aka The Heavy Water War) - 01x06 - Episode 6 (Finale).TVC (HDTV).English.C.orig.Addic7ed.com
		Pattern p107 = Pattern.compile(srSsnEpiNumsPttrn + "(.+?)\\.(\\w+)\\s+\\((.*?)\\)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps107 = ImmutableMap.builder();
		grps107.put(0, SubtitleRelease.PROP_NAME);
		grps107.put(1, Series.PROP_NAME);
		grps107.put(2, Series.PROP_TITLE);
		grps107.put(3, Series.PROP_DATE); // e.g. "2004"
		grps107.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps107.put(5, Season.PROP_NUMBER);
		grps107.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps107.put(7, Episode.PROP_TITLE);
		grps107.put(8, Release.PROP_GROUP);
		grps107.put(9, Release.PROP_TAGS);
		grps107.put(10, Subtitle.PROP_LANGUAGE);
		grps107.put(11, SubtitleRelease.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher107 = new PatternMappingMatcher<>(p107, grps107.build(), predefEpisodeMatches);
		episodeMatchers.add(matcher107);

		// TODO: Actually "(HDTV + 720p)" are 2 releases "HDTV" and "720p.HDTV". Two releases should be matched.
		// But thats not possible with current matching algorithm. It has to be possible to numerate the objects if multiple

		SubtitleReleaseParser episodeSubParser = new SubtitleReleaseParser(new MultiMappingMatcher<>(episodeMatchers.build()), ParsingDefaults.getDefaultSingletonListEpisodeMapper());

		// FOR TESTING
		// matcher104.match("Hannibal - 03x10 - ...And the Woman Clothed in Sun.WEB-DL.English.HI.C.orig.Addic7ed.com").forEach((k, v) ->
		// System.out.println(k + " = " + v));

		// --------------
		// Movie Parser
		ImmutableMap.Builder<SimplePropDescriptor, String> predefMovieMatchesBuilder = ImmutableMap.builder();
		predefMovieMatchesBuilder.putAll(commonPredefMatches);
		ImmutableMap<SimplePropDescriptor, String> predefMovieMatches = predefMovieMatchesBuilder.build();

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> movieMatchers = ImmutableList.builder();

		// "Winter's Tale (2014).DVD-Rip.English.orig.Addic7ed.com"
		Pattern p201 = Pattern.compile("(.*?)\\s+\\((\\d{4})\\)\\.(" + rlsTagsPttrn + ")\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(0, SubtitleRelease.PROP_NAME);
		grps201.put(1, Movie.PROP_NAME);
		grps201.put(2, Movie.PROP_DATE);
		grps201.put(3, Release.PROP_TAGS);
		grps201.put(4, Subtitle.PROP_LANGUAGE);
		grps201.put(5, SubtitleRelease.PROP_TAGS);
		PatternMappingMatcher<SimplePropDescriptor> matcher201 = new PatternMappingMatcher<>(p201, grps201.build(), predefMovieMatches);
		movieMatchers.add(matcher201);

		// "The Man Behind the Throne (2013).CBFM.English.C.orig.Addic7ed.com"
		Pattern p202 = Pattern.compile("(.*?)\\s+\\((\\d{4})\\)\\.(\\w+)\\." + langSubTagsSrcPttrn, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps202 = ImmutableMap.builder();
		grps202.put(0, SubtitleRelease.PROP_NAME);
		grps202.put(1, Movie.PROP_NAME);
		grps202.put(2, Movie.PROP_DATE);
		grps202.put(3, Release.PROP_GROUP);
		grps202.put(4, Subtitle.PROP_LANGUAGE);
		grps202.put(5, SubtitleRelease.PROP_TAGS);
		PatternMappingMatcher<SimplePropDescriptor> matcher202 = new PatternMappingMatcher<>(p202, grps202.build(), predefMovieMatches);
		movieMatchers.add(matcher202);

		SubtitleReleaseParser movieSubParser = new SubtitleReleaseParser(new MultiMappingMatcher<>(movieMatchers.build()), ParsingDefaults.getDefaultSingletonListMovieMapper());

		return ImmutableList.of(episodeSubParser, movieSubParser);
	}

	public static final ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static List<ParserEntry<?>> getParserEntries()
	{
		return PARSING_SERVICE.getParserEntries();
	}

	private Addic7edCom2()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
