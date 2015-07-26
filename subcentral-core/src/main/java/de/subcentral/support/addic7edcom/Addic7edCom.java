package de.subcentral.support.addic7edcom;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.media.RegularMedia;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingDefaults;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.parsing.TypeParsingService;
import de.subcentral.core.parsing.TypeParsingService.ParserEntry;
import de.subcentral.core.util.SimplePropDescriptor;

public class Addic7edCom
{
	public static final String DOMAIN = "addic7ed.com";

	private static final TypeParsingService PARSING_SERVICE = new TypeParsingService(DOMAIN);

	static
	{
		PARSING_SERVICE.registerAllParsers(SubtitleAdjustment.class, initParsers());
	}

	private static List<Parser<SubtitleAdjustment>> initParsers()
	{
		// Common Objects
		String seriesSeasonEpiNumsPattern = ParsingDefaults.PATTERN_MEDIA_NAME + " - (\\d{2})x(\\d{2}) - ";
		// String tagsPattern = "((HI|C|orig|updated)+)";
		String langPattern = "(Albanian|Arabic|Armenian|Azerbaijani|Bengali|Bosnian|Bulgarian|Català|Chinese \\(Simplified\\)|Chinese \\(Traditional\\)|Croatian|Czech|Danish|Dutch|English|Euskera|Finnish|French|Galego|German|Greek|Hebrew|Hungarian|Indonesian|Italian|Japanese|Korean|Macedonian|Malay|Norwegian|Persian|Polish|Portuguese|Portuguese \\(Brazilian\\)|Romanian|Russian|Serbian \\(Cyrillic\\)|Serbian \\(Latin\\)|Slovak|Slovenian|Spanish|Spanish \\(Latin America\\)|Spanish \\(Spain\\)|Swedish|Thai|Turkish|Ukrainian|Vietnamese)";
		String langTagsSourcePattern = langPattern + "\\.(.+)\\.Addic7ed\\.com";
		ImmutableMap<SimplePropDescriptor, String> commonPredefMatches = ImmutableMap.of(Subtitle.PROP_SOURCE, "Addic7ed.com", RegularMedia.PROP_MEDIA_CONTENT_TYPE, Media.MEDIA_CONTENT_TYPE_VIDEO);

		// --------------
		// Episode Parser
		// Predefined matches for episodes
		SubtitleAdjustmentParser episodeSubParser = new SubtitleAdjustmentParser(ParsingDefaults.getDefaultSingletonListEpisodeMapper());

		ImmutableMap.Builder<SimplePropDescriptor, String> predefEpisodeMatchesBuilder = ImmutableMap.builder();
		predefEpisodeMatchesBuilder.putAll(commonPredefMatches);
		predefEpisodeMatchesBuilder.put(Series.PROP_TYPE, Series.TYPE_SEASONED);
		ImmutableMap<SimplePropDescriptor, String> predefEpisodeMatches = predefEpisodeMatchesBuilder.build();

		// WEB-DL but then no "-" after that (which would indicate a group)
		// Examples:
		// Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com
		Pattern p101 = Pattern.compile(seriesSeasonEpiNumsPattern + "(.+?)\\.(WEB[.-]DL\\.[\\w\\.]+)\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, SubtitleAdjustment.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Series.PROP_TITLE);
		grps101.put(3, Series.PROP_DATE); // e.g. "2004"
		grps101.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps101.put(5, Season.PROP_NUMBER);
		grps101.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(7, Episode.PROP_TITLE);
		grps101.put(8, Release.PROP_TAGS);
		grps101.put(9, Subtitle.PROP_LANGUAGE);
		grps101.put(10, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<>(p101, grps101.build(), predefEpisodeMatches);

		// Expecting no dots in the episode title, then a dot, then "WEB-DL" or a group
		// Examples:
		// "Psych - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com"
		Pattern p102 = Pattern.compile(seriesSeasonEpiNumsPattern + "([^\\.]+?)\\.(?:(WEB[.-]DL)|(\\w+))\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(0, SubtitleAdjustment.PROP_NAME);
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Series.PROP_TITLE);
		grps102.put(3, Series.PROP_DATE); // e.g. "2004"
		grps102.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps102.put(5, Season.PROP_NUMBER);
		grps102.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(7, Episode.PROP_TITLE);
		grps102.put(8, Release.PROP_TAGS); // WEB-DL
		grps102.put(9, Release.PROP_GROUP);
		grps102.put(10, Subtitle.PROP_LANGUAGE);
		grps102.put(11, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher102 = new MappingMatcher<>(p102, grps102.build(), predefEpisodeMatches);

		// Expecting no dots in the episode title, then a dot, then release tags, then a non-wordchar delimiter, then "WEB-DL" or a group.
		// Examples:
		// "10 Things I Hate About You - 01x01 - Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com"
		// "Switched At Birth - 03x04 - It Hurts to Wait With Love If Love Is Somewhere Else.HDTV.x264-EXCELLENCE.Dutch.orig.Addic7ed.com"
		// "Vikings - 01x08 - Sacrifice.x264.2HD.English.C.orig.Addic7ed.com"
		// "Out There (2013) - 01x09 - Viking Days.480p.WEB-DL.x264-mSD.English.C.orig.Addic7ed.com"
		// "Psych - 01x01 - Pilot.DVDRip TOPAZ.French.orig.Addic7ed.com"
		Pattern p103 = Pattern.compile(seriesSeasonEpiNumsPattern + "([^\\.]+?)\\.([\\w+\\.-]+?)\\W(?:(WEB[.-]DL)|(\\w+))\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps103 = ImmutableMap.builder();
		grps103.put(0, SubtitleAdjustment.PROP_NAME);
		grps103.put(1, Series.PROP_NAME);
		grps103.put(2, Series.PROP_TITLE);
		grps103.put(3, Series.PROP_DATE); // e.g. "2004"
		grps103.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps103.put(5, Season.PROP_NUMBER);
		grps103.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps103.put(7, Episode.PROP_TITLE);
		grps103.put(8, Release.PROP_TAGS);
		grps103.put(9, Release.PROP_TAGS);
		grps103.put(10, Release.PROP_GROUP);
		grps103.put(11, Subtitle.PROP_LANGUAGE);
		grps103.put(12, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher103 = new MappingMatcher<>(p103, grps103.build(), predefEpisodeMatches);

		// Episode title may contain dots, then a dot, then release tags, then a non-wordchar delimiter, then "WEB-DL" or a group
		// Examples:
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264.DIMENSION.English.HI.Addic7ed.com
		// 10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com
		Pattern p104 = Pattern.compile(seriesSeasonEpiNumsPattern + "(.+?)\\.([\\w+\\.-]+?)\\W(?:(WEB[.-]DL)|(\\w+))\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps104 = ImmutableMap.builder();
		grps104.put(0, SubtitleAdjustment.PROP_NAME);
		grps104.put(1, Series.PROP_NAME);
		grps104.put(2, Series.PROP_TITLE);
		grps104.put(3, Series.PROP_DATE); // e.g. "2004"
		grps104.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps104.put(5, Season.PROP_NUMBER);
		grps104.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps104.put(7, Episode.PROP_TITLE);
		grps104.put(8, Release.PROP_TAGS);
		grps104.put(9, Release.PROP_TAGS);
		grps104.put(10, Release.PROP_GROUP);
		grps104.put(11, Subtitle.PROP_LANGUAGE);
		grps104.put(12, SubtitleAdjustment.PROP_TAGS);

		MappingMatcher<SimplePropDescriptor> matcher104 = new MappingMatcher<>(p104, grps104.build(), predefEpisodeMatches);

		// Episode title may contain dots, then a dot, then "WEB-DL" or a group
		// Examples:
		// "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com"
		// "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com"
		// "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com"
		Pattern p105 = Pattern.compile(seriesSeasonEpiNumsPattern + "(.+?)\\.(?:(WEB[.-]DL)|(\\w+))\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps105 = ImmutableMap.builder();
		grps105.put(0, SubtitleAdjustment.PROP_NAME);
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
		grps105.put(11, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher105 = new MappingMatcher<>(p105, grps105.build(), predefEpisodeMatches);

		// Multiple release groups (and hence matching releases) separated by comma
		// Examples
		// "Finding Carter - 01x07 - Throw Momma From the Train.KILLERS, MSD.English.C.orig.Addic7ed.com"
		Pattern p106 = Pattern.compile(seriesSeasonEpiNumsPattern + "(.+?)\\.(\\w+(?:,\\s+\\w+)*)\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps106 = ImmutableMap.builder();
		grps106.put(0, SubtitleAdjustment.PROP_NAME);
		grps106.put(1, Series.PROP_NAME);
		grps106.put(2, Series.PROP_TITLE);
		grps106.put(3, Series.PROP_DATE); // e.g. "2004"
		grps106.put(4, Series.PROP_COUNTRIES); // e.g. "UK"
		grps106.put(5, Season.PROP_NUMBER);
		grps106.put(6, Episode.PROP_NUMBER_IN_SEASON);
		grps106.put(7, Episode.PROP_TITLE);
		grps106.put(8, Release.PROP_GROUP);
		grps106.put(9, Subtitle.PROP_LANGUAGE);
		grps106.put(10, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher106 = new MappingMatcher<>(p106, grps106.build(), predefEpisodeMatches);

		// FOR TESTING
		// matcher102.match("Psych (UK) - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com").forEach((k, v) -> System.out.println(k
		// + " = " + v));

		// Release group, then tags in parenthesis
		// Death in Paradise - 04x04 - Series 4, Episode 4.FoV (HDTV + 720p).English.C.orig.Addic7ed.com
		// The Saboteurs (aka The Heavy Water War) - 01x06 - Episode 6 (Finale).TVC (HDTV).English.C.orig.Addic7ed.com
		Pattern p107 = Pattern.compile(seriesSeasonEpiNumsPattern + "(.+?)\\.(\\w+)\\s+\\((.*?)\\)\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps107 = ImmutableMap.builder();
		grps107.put(0, SubtitleAdjustment.PROP_NAME);
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
		grps107.put(11, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher107 = new MappingMatcher<>(p107, grps107.build(), predefEpisodeMatches);

		// TODO: Actually "(HDTV + 720p)" are 2 releases "HDTV" and "720p.HDTV". Two releases should be matched.
		// But thats not possible with current matching algorithm. It has to be possible to numerate the objects if multiple

		// Matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> episodeMatchers = ImmutableList.builder();
		episodeMatchers.add(matcher101);
		episodeMatchers.add(matcher102);
		episodeMatchers.add(matcher103);
		episodeMatchers.add(matcher104);
		episodeMatchers.add(matcher105);
		episodeMatchers.add(matcher106);
		episodeMatchers.add(matcher107);
		episodeSubParser.setMatchers(episodeMatchers.build());

		// --------------
		// Movie Parser
		SubtitleAdjustmentParser movieSubParser = new SubtitleAdjustmentParser(ParsingDefaults.getDefaultSingletonListRegularMediaMapper());

		ImmutableMap.Builder<SimplePropDescriptor, String> predefMovieMatchesBuilder = ImmutableMap.builder();
		predefMovieMatchesBuilder.putAll(commonPredefMatches);
		predefMovieMatchesBuilder.put(RegularMedia.PROP_MEDIA_TYPE, Media.MEDIA_TYPE_MOVIE);
		ImmutableMap<SimplePropDescriptor, String> predefMovieMatches = predefEpisodeMatchesBuilder.build();

		// "Winter's Tale (2014).DVD-Rip.English.orig.Addic7ed.com"
		Pattern p201 = Pattern.compile("((.*?) \\((\\d{4})\\))\\.([\\w-]+)\\." + langTagsSourcePattern, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps201 = ImmutableMap.builder();
		grps201.put(0, SubtitleAdjustment.PROP_NAME);
		grps201.put(1, RegularMedia.PROP_NAME);
		grps201.put(2, RegularMedia.PROP_TITLE);
		grps201.put(3, RegularMedia.PROP_DATE);
		grps201.put(4, Release.PROP_TAGS);
		grps201.put(5, Subtitle.PROP_LANGUAGE);
		grps201.put(6, SubtitleAdjustment.PROP_TAGS);
		MappingMatcher<SimplePropDescriptor> matcher201 = new MappingMatcher<>(p201, grps201.build(), predefMovieMatches);

		// --------------
		// add all movie matchers
		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> movieMatchers = ImmutableList.builder();
		movieMatchers.add(matcher201);

		movieSubParser.setMatchers(movieMatchers.build());

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

	private Addic7edCom()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
