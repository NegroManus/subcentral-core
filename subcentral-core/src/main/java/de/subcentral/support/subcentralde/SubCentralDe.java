package de.subcentral.support.subcentralde;

import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.AvMedia;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.MappingMatcherExtension;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.parsing.TypeParsingService;
import de.subcentral.core.parsing.TypeParsingService.ParserEntry;
import de.subcentral.core.standardizing.PatternStringReplacer;
import de.subcentral.core.standardizing.PatternSubtitleLanguageStandardizer;
import de.subcentral.core.standardizing.TypeStandardizingService;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.releasescene.ReleaseScene;

public class SubCentralDe
{
	public static final String				DOMAIN			= "subcentral.de";

	private static final Logger				log				= LogManager.getLogger(SubCentralDe.class.getName());
	private static final TypeParsingService	PARSING_SERVICE	= new TypeParsingService(DOMAIN);
	static
	{
		PARSING_SERVICE.registerAllParsers(SubtitleAdjustment.class, initParsers());
	}

	@SuppressWarnings("unchecked")
	private static List<Parser<SubtitleAdjustment>> initParsers()
	{
		List<MappingMatcherExtension> scExtensions = initExtensions();

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
			List<MappingMatcher<SimplePropDescriptor>> sceneMatchers = sceneRlsParser.getMatchers();
			ImmutableList.Builder<MappingMatcher<SimplePropDescriptor>> scMatchers = ImmutableList.builder();
			for (MappingMatcherExtension extension : scExtensions)
			{
				scMatchers.addAll(extension.extend(sceneMatchers));
			}

			parser.setMatchers(scMatchers.build());
			parsers.add(parser);
		}

		return parsers.build();
	}

	private static List<MappingMatcherExtension> initExtensions()
	{
		String langGrp = "\\W(de|ger|german|deutsch|VO|en|eng|english)";
		String groupGrp = "\\W([\\w&_]+)";
		String versionGrp = "\\W(V\\d)";

		ImmutableList.Builder<MappingMatcherExtension> extensions = ImmutableList.builder();

		// Revision, Language, (Group)?
		MappingMatcherExtension ext01 = new MappingMatcherExtension();
		ext01.setPatternPrefix("(");
		ext01.setPrefixProps(ImmutableList.of(SubtitleAdjustment.PROP_NAME));
		ext01.setPatternSuffix(")" + versionGrp + langGrp + "(?:" + groupGrp + ")?");
		ext01.setSuffixProps(ImmutableList.of(SubtitleAdjustment.PROP_REVISION, Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP));
		extensions.add(ext01);

		// Language, Revision, (Group)?
		MappingMatcherExtension ext02 = new MappingMatcherExtension();
		ext02.setPatternPrefix("(");
		ext02.setPrefixProps(ImmutableList.of(SubtitleAdjustment.PROP_NAME));
		ext02.setPatternSuffix(")" + langGrp + versionGrp + "(?:" + groupGrp + ")?");
		ext02.setSuffixProps(ImmutableList.of(Subtitle.PROP_LANGUAGE, SubtitleAdjustment.PROP_REVISION, Subtitle.PROP_GROUP));
		extensions.add(ext02);

		// Language, Group, (Revision)?
		MappingMatcherExtension ext03 = new MappingMatcherExtension();
		ext03.setPatternPrefix("(");
		ext03.setPrefixProps(ImmutableList.of(SubtitleAdjustment.PROP_NAME));
		ext03.setPatternSuffix(")" + langGrp + groupGrp + "(?:" + versionGrp + ")?");
		ext03.setSuffixProps(ImmutableList.of(Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP, SubtitleAdjustment.PROP_REVISION));
		extensions.add(ext03);

		// Language, (Group)?
		MappingMatcherExtension ext04 = new MappingMatcherExtension();
		ext04.setPatternPrefix("(");
		ext04.setPrefixProps(ImmutableList.of(SubtitleAdjustment.PROP_NAME));
		ext04.setPatternSuffix(")" + langGrp + "(?:" + groupGrp + ")?");
		ext04.setSuffixProps(ImmutableList.of(Subtitle.PROP_LANGUAGE, Subtitle.PROP_GROUP));
		extensions.add(ext04);

		// Revision
		MappingMatcherExtension ext05 = new MappingMatcherExtension();
		ext05.setPatternPrefix("(");
		ext05.setPrefixProps(ImmutableList.of(SubtitleAdjustment.PROP_NAME));
		ext05.setPatternSuffix(")" + versionGrp);
		ext05.setSuffixProps(ImmutableList.of(SubtitleAdjustment.PROP_REVISION));
		extensions.add(ext05);

		return extensions.build();
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static List<ParserEntry<?>> getParserEntries()
	{
		return PARSING_SERVICE.getParserEntries();
	}

	public static void registerSubtitleLanguageStandardizers(TypeStandardizingService service)
	{
		service.registerStandardizer(Subtitle.class,
				new PatternSubtitleLanguageStandardizer(new PatternStringReplacer(Pattern.compile("(en|eng|english)", Pattern.CASE_INSENSITIVE), "VO")));
		service.registerStandardizer(Subtitle.class,
				new PatternSubtitleLanguageStandardizer(new PatternStringReplacer(Pattern.compile("(ger|german|deu|deutsch)",
						Pattern.CASE_INSENSITIVE), "de")));
	}

	private SubCentralDe()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
