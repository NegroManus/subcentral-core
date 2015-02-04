package de.subcentral.support.subcentralde;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.metadata.media.AvMedia;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.ClassBasedParsingService;
import de.subcentral.core.parsing.ClassBasedParsingService.ParserEntry;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.standardizing.ClassBasedStandardizingService;
import de.subcentral.core.standardizing.SubtitleLanguageCustomStandardizer;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.releasescene.ReleaseScene;

public class SubCentralDe
{
	public static final String						DOMAIN			= "subcentral.de";

	private static final Logger						log				= LogManager.getLogger(SubCentralDe.class.getName());
	private static final ClassBasedParsingService	PARSING_SERVICE	= new ClassBasedParsingService(DOMAIN);
	static
	{
		PARSING_SERVICE.registerAllParsers(SubtitleAdjustment.class, initParsers());
	}

	@SuppressWarnings("unchecked")
	private static List<Parser<SubtitleAdjustment>> initParsers()
	{
		String scPatternPrefix = "(";
		String scPatternSuffix = ")\\.(de|ger|german|VO|en|english)(?:-|\\.)([\\w&]+)";

		ImmutableList.Builder<Parser<SubtitleAdjustment>> parsers = ImmutableList.builder();

		for (ParserEntry<?> sceneParserEntry : ReleaseScene.getParsersEntries())
		{
			Parser<?> sceneParser = sceneParserEntry.getParser();
			if (!(sceneParser instanceof ReleaseParser))
			{
				log.warn("Parser will be ignored because it is not an instance of ReleaseParser: {}", sceneParser);
				continue;
			}
			ReleaseParser sceneRlsParser = (ReleaseParser) sceneParser;

			Mapper<List<AvMedia>> mediaMapper;
			try
			{
				mediaMapper = (Mapper<List<AvMedia>>) sceneRlsParser.getMediaMapper();
			}
			catch (ClassCastException e)
			{
				log.warn("Parser will be ignored because its media mapper does not map to List<AvMedia>: {}", sceneRlsParser.getMediaMapper());
				continue;
			}

			SubtitleAdjustmentParser parser = new SubtitleAdjustmentParser(mediaMapper);

			// Building the matchers for SubCentral:
			// The scene matchers will be the source for the SubCentral matchers
			// because all SubCentral names consist of the scene name of the release followed by SubCentral tags.
			// The properties of the SubCentral matchers are constructed as follows:
			// pattern: "(" + scene pattern + ")" + SubCentral pattern extension
			// -> the () encapture the release name
			// groups: scene groups + SubCentral groups
			// predefinedMatches: scene predefinedMatches
			List<MappingMatcher<SimplePropDescriptor>> sceneMatchers = sceneRlsParser.getMatchers();
			ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();
			for (MappingMatcher<SimplePropDescriptor> sceneMatcher : sceneMatchers)
			{
				int highestGroupNum = sceneMatcher.getGroups().keySet().stream().mapToInt(i -> i.intValue()).max().getAsInt();
				Pattern p = Pattern.compile(scPatternPrefix + sceneMatcher.getPattern() + scPatternSuffix, Pattern.CASE_INSENSITIVE);
				ImmutableMap.Builder<Integer, SimplePropDescriptor> grps = ImmutableMap.builder();
				grps.put(0, SubtitleAdjustment.PROP_NAME);
				// the release name is now the first group
				grps.put(1, Release.PROP_NAME);
				for (Map.Entry<Integer, SimplePropDescriptor> sceneGrp : sceneMatcher.getGroups().entrySet())
				{
					if (Integer.valueOf(0).equals(sceneGrp.getKey()))
					{
						// the group 0 is removed because it captured the whole string as the release name
						// now it is the name of the SubtitleAdjustment
						continue;
					}
					// because there is a new group 1 (release name),
					// all other group numbers have to be increased by 1
					grps.put(sceneGrp.getKey() + 1, sceneGrp.getValue());
				}
				// the additional groups have to continue their count at highestGroupNum + 1 (for new group 1) + 1
				grps.put(highestGroupNum + 2, Subtitle.PROP_LANGUAGE);
				grps.put(highestGroupNum + 3, Subtitle.PROP_GROUP);
				ImmutableMap.Builder<SimplePropDescriptor, String> predefinedMatches = ImmutableMap.builder();
				predefinedMatches.putAll(sceneMatcher.getPredefinedMatches());
				predefinedMatches.put(Subtitle.PROP_SOURCE, "SubCentral.de");
				MappingMatcher<SimplePropDescriptor> matcher = new MappingMatcher<SimplePropDescriptor>(p, grps.build(), predefinedMatches.build());
				matchers.add(matcher);
			}

			parser.setMatchers(matchers.build());
			parsers.add(parser);
		}

		return parsers.build();
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static List<ParserEntry<?>> getParserEntries()
	{
		return PARSING_SERVICE.getParserEntries();
	}

	public static void registerSubtitleLanguageStandardizers(ClassBasedStandardizingService service)
	{
		service.registerStandardizer(Subtitle.class, new SubtitleLanguageCustomStandardizer(Pattern.compile("(en|eng|english)", Pattern.CASE_INSENSITIVE),
				"VO"));
		service.registerStandardizer(Subtitle.class,
				new SubtitleLanguageCustomStandardizer(Pattern.compile("(ger|german|deu|deutsch)", Pattern.CASE_INSENSITIVE), "de"));
	}

	private SubCentralDe()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
