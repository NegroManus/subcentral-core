package de.subcentral.support.subcentralde;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.subcentral.core.correct.PatternStringReplacer;
import de.subcentral.core.correct.SubtitleLanguageCorrector;
import de.subcentral.core.correct.TypeBasedCorrectionService;
import de.subcentral.core.metadata.media.Media;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
import de.subcentral.core.parse.DelegatingMappingMatcher;
import de.subcentral.core.parse.DelegatingMappingMatcher.GroupEntry;
import de.subcentral.core.parse.DelegatingMappingMatcher.KeyEntry;
import de.subcentral.core.parse.DelegatingMappingMatcher.MatcherEntry;
import de.subcentral.core.parse.Mapper;
import de.subcentral.core.parse.MappingMatcher;
import de.subcentral.core.parse.MultiMappingMatcher;
import de.subcentral.core.parse.Parser;
import de.subcentral.core.parse.ParsingService;
import de.subcentral.core.parse.ReleaseParser;
import de.subcentral.core.parse.SubtitleReleaseParser;
import de.subcentral.core.parse.TypeBasedParsingService;
import de.subcentral.core.parse.TypeBasedParsingService.ParserEntry;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.releasescene.ReleaseScene;

public class SubCentralDe
{
	public static final String						SITE_ID			= "subcentral.de";

	private static final Logger						log				= LogManager.getLogger(SubCentralDe.class.getName());
	private static final TypeBasedParsingService	PARSING_SERVICE	= new TypeBasedParsingService(SITE_ID);

	static
	{
		PARSING_SERVICE.registerAll(SubtitleRelease.class, initParsers());
	}

	@SuppressWarnings("unchecked")
	private static List<Parser<SubtitleRelease>> initParsers()
	{
		ImmutableList.Builder<Parser<SubtitleRelease>> parsers = ImmutableList.builder();

		for (ParserEntry<?> sceneParserEntry : ReleaseScene.getParsersEntries())
		{
			Parser<?> sceneParser = sceneParserEntry.getParser();
			if (!(sceneParser instanceof ReleaseParser))
			{
				log.warn("Parser will be ignored because it is not an instance of ReleaseParser: {}", sceneParser);
				continue;
			}
			ReleaseParser sceneRlsParser = (ReleaseParser) sceneParser;

			Mapper<List<Media>> mediaMapper;
			try
			{
				mediaMapper = (Mapper<List<Media>>) sceneRlsParser.getMediaMapper();
			}
			catch (ClassCastException e)
			{
				log.warn("Parser will be ignored because its media mapper does not map to List<Media>: {}", sceneRlsParser.getMediaMapper());
				continue;
			}

			// Building the matchers for SubCentral:
			// The scene matchers will be the source for the SubCentral matchers
			// because all SubCentral names consist of the scene name of the release followed by SubCentral tags.
			MappingMatcher<SimplePropDescriptor> scMatcher = buildMatcherBasedOnSceneMatcher(sceneRlsParser.getMatcher());

			SubtitleReleaseParser parser = new SubtitleReleaseParser(scMatcher, mediaMapper);
			parsers.add(parser);
		}

		return parsers.build();
	}

	private static MappingMatcher<SimplePropDescriptor> buildMatcherBasedOnSceneMatcher(MappingMatcher<SimplePropDescriptor> sceneMatcher)
	{
		String release = "(.*)";
		String lang = "\\W(de|ger|german|deutsch|VO|en|eng|english)";
		String group = "\\W([\\w&_]+)";
		String groupOpt = "(?:" + group + ")?";
		String version = "\\W(V\\d)";
		String versionOpt = "(?:" + version + ")?";

		ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> matchers = ImmutableList.builder();

		// Version, Language, (Group)?
		Pattern p101 = Pattern.compile(release + version + lang + groupOpt, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g101 = ImmutableMap.builder();
		g101.put(0, KeyEntry.of(SubtitleRelease.PROP_NAME));
		g101.put(1, MatcherEntry.of(sceneMatcher));
		g101.put(2, KeyEntry.of(SubtitleRelease.PROP_VERSION));
		g101.put(3, KeyEntry.of(Subtitle.PROP_LANGUAGE));
		g101.put(4, KeyEntry.of(Subtitle.PROP_GROUP));
		MappingMatcher<SimplePropDescriptor> m101 = new DelegatingMappingMatcher<>(p101, g101.build());
		matchers.add(m101);

		// Language, Version, (Group)?
		Pattern p102 = Pattern.compile(release + lang + version + groupOpt, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g102 = ImmutableMap.builder();
		g102.put(0, KeyEntry.of(SubtitleRelease.PROP_NAME));
		g102.put(1, MatcherEntry.of(sceneMatcher));
		g102.put(2, KeyEntry.of(Subtitle.PROP_LANGUAGE));
		g102.put(3, KeyEntry.of(SubtitleRelease.PROP_VERSION));
		g102.put(4, KeyEntry.of(Subtitle.PROP_GROUP));
		MappingMatcher<SimplePropDescriptor> m2 = new DelegatingMappingMatcher<>(p102, g102.build());
		matchers.add(m2);

		// Language, Group, (Version)?
		Pattern p103 = Pattern.compile(release + lang + group + versionOpt, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g103 = ImmutableMap.builder();
		g103.put(0, KeyEntry.of(SubtitleRelease.PROP_NAME));
		g103.put(1, MatcherEntry.of(sceneMatcher));
		g103.put(2, KeyEntry.of(Subtitle.PROP_LANGUAGE));
		g103.put(3, KeyEntry.of(Subtitle.PROP_GROUP));
		g103.put(4, KeyEntry.of(SubtitleRelease.PROP_VERSION));
		MappingMatcher<SimplePropDescriptor> m103 = new DelegatingMappingMatcher<>(p103, g103.build());
		matchers.add(m103);

		// Language, (Group)?
		Pattern p104 = Pattern.compile(release + lang + groupOpt, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g104 = ImmutableMap.builder();
		g104.put(0, KeyEntry.of(SubtitleRelease.PROP_NAME));
		g104.put(1, MatcherEntry.of(sceneMatcher));
		g104.put(2, KeyEntry.of(Subtitle.PROP_LANGUAGE));
		g104.put(3, KeyEntry.of(Subtitle.PROP_GROUP));
		MappingMatcher<SimplePropDescriptor> m104 = new DelegatingMappingMatcher<>(p104, g104.build());
		matchers.add(m104);

		// Version
		Pattern p105 = Pattern.compile(release + version, Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, GroupEntry<SimplePropDescriptor>> g105 = ImmutableMap.builder();
		g105.put(0, KeyEntry.of(SubtitleRelease.PROP_NAME));
		g105.put(1, MatcherEntry.of(sceneMatcher));
		g105.put(2, KeyEntry.of(SubtitleRelease.PROP_VERSION));
		MappingMatcher<SimplePropDescriptor> m105 = new DelegatingMappingMatcher<>(p105, g105.build());
		matchers.add(m105);

		return new MultiMappingMatcher<>(matchers.build());
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static List<ParserEntry<?>> getParserEntries()
	{
		return PARSING_SERVICE.getParserEntries();
	}

	public static void registerSubtitleLanguageCorrectors(TypeBasedCorrectionService service)
	{
		service.registerCorrector(Subtitle.class, new SubtitleLanguageCorrector(new PatternStringReplacer(Pattern.compile("(en|eng|english)", Pattern.CASE_INSENSITIVE), "VO")));
		service.registerCorrector(Subtitle.class, new SubtitleLanguageCorrector(new PatternStringReplacer(Pattern.compile("(ger|german|deu|deutsch)", Pattern.CASE_INSENSITIVE), "de")));
	}

	private SubCentralDe()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
