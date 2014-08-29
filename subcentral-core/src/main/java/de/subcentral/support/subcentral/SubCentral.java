package de.subcentral.support.subcentral;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.parsing.MappingMatcher;
import de.subcentral.core.parsing.Parser;
import de.subcentral.core.parsing.ParsingService;
import de.subcentral.core.parsing.SimpleParsingService;
import de.subcentral.core.parsing.SubtitleAdjustmentParser;
import de.subcentral.core.standardizing.SimpleStandardizingService;
import de.subcentral.core.standardizing.Standardizer;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.support.scene.Scene;

public class SubCentral
{
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();
	static
	{
		PARSING_SERVICE.setParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		String scPatternPrefix = "(";
		String scPatternSuffix = ")\\.(de|ger|german|VO|en|english)(?:-|\\.)([\\w&]+)";

		SubtitleAdjustmentParser epiParser = new SubtitleAdjustmentParser("subcentral.de");

		// Building the matchers for SubCentral:
		// The scene matchers will be the source for the SubCentral matchers
		// because all SubCentral names consist of the scene name of the release followed by SubCentral tags.
		// The properties of the SubCentral matchers are constructed as follows:
		// pattern: "(" + scene pattern + ")" + SubCentral pattern extension
		// -> the () encapture the release name
		// groups: scene groups + SubCentral groups
		// predefinedMatches: scene predefinedMatches
		List<MappingMatcher<SimplePropDescriptor>> sceneMatchers = Scene.getAllMatchers();
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
				if (sceneGrp.getKey() == Integer.valueOf(0))
				{
					// the group 0 is removed because it captured the whole string as the release name
					// not it is the name of the SubtitleAdjustment
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

		epiParser.setMatchers(matchers.build());

		SimpleStandardizingService ss = new SimpleStandardizingService();
		ImmutableListMultimap.Builder<Class<?>, Standardizer<?>> standardizers = ImmutableListMultimap.builder();
		standardizers.put(Episode.class, (Episode e) -> Scene.removeDotsInEpisode(e));
		standardizers.put(Movie.class, (Movie m) -> Scene.removeDotsInMovie(m));
		standardizers.put(Release.class, (Release r) -> Releases.standardizeTags(r));
		standardizers.put(Subtitle.class, (Subtitle s) -> standardizeSubtitleLanguage(s));
		ss.setStandardizers(standardizers.build());
		epiParser.setStandardizingService(ss);

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
