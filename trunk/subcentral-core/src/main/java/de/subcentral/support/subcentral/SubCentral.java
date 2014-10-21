package de.subcentral.support.subcentral;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.AvMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.Mapper;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.ReleaseParser;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.scene.Scene;

public class SubCentral
{
	public static final String					DOMAIN			= "subcentral.de";

	private static final Logger					log				= LogManager.getLogger(SubCentral.class.getName());
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();
	static
	{
		PARSING_SERVICE.setParsers(initParsers());
	}

	@SuppressWarnings("unchecked")
	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		String scPatternPrefix = "(";
		String scPatternSuffix = ")\\.(de|ger|german|VO|en|english)(?:-|\\.)([\\w&]+)";

		ImmutableListMultimap.Builder<Class<?>, Parser<?>> parsers = ImmutableListMultimap.builder();

		for (Parser<?> sceneParser : Scene.getAllParsers().get(Release.class))
		{
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

			SubtitleAdjustmentParser parser = new SubtitleAdjustmentParser(sceneRlsParser.getDomain().replace(Scene.DOMAIN, DOMAIN), mediaMapper);

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
				predefinedMatches.put(Subtitle.PROP_SOURCE_URL, "http://www.subcentral.de");
				MappingMatcher<SimplePropDescriptor> matcher = new MappingMatcher<SimplePropDescriptor>(p, grps.build(), predefinedMatches.build());
				matchers.add(matcher);
			}

			parser.setMatchers(matchers.build());
			parsers.put(SubtitleAdjustment.class, parser);
		}

		return parsers.build();
	}

	public static ParsingService getParsingService()
	{
		return PARSING_SERVICE;
	}

	public static ListMultimap<Class<?>, Parser<?>> getAllParsers()
	{
		return PARSING_SERVICE.getParsers();
	}

	public static void standardizeSubtitleLanguage(Subtitle sub)
	{
		if (sub == null)
		{
			return;
		}
		String lang = sub.getLanguage();
		if (lang == null)
		{
			return;
		}
		if (lang.equalsIgnoreCase("en") || lang.equalsIgnoreCase("eng") || lang.equalsIgnoreCase("english"))
		{
			lang = "VO";
		}
		else if (lang.equalsIgnoreCase("ger") || lang.equalsIgnoreCase("german") || lang.equalsIgnoreCase("deu") || lang.equalsIgnoreCase("deutsch"))
		{
			lang = "de";
		}
		sub.setLanguage(lang);
	}

	private SubCentral()
	{
		// utility class
	}

}
