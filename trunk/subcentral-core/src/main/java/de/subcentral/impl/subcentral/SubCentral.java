package de.subcentral.impl.subcentral;

import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
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
import de.subcentral.impl.scene.Scene;

public class SubCentral
{
	private static final SimpleParsingService	PARSING_SERVICE	= new SimpleParsingService();
	static
	{
		PARSING_SERVICE.setParsers(initParsers());
	}

	private static ListMultimap<Class<?>, Parser<?>> initParsers()
	{
		String tagsPattern = Scene.buildFirstTagPattern();

		SubtitleAdjustmentParser epiParser = new SubtitleAdjustmentParser("subcentral.de");

		// Seasoned Episode matchers

		// Psych.S08E07.Shawn.and.Gus.Truck.Things.Up.720p.WEB-DL.DD5.1.H.264-ECI.de-SubCentral
		Pattern p101 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)\\.(" + tagsPattern
				+ "\\..*)-(\\w+)\\.(de|ger|german|VO|en|english)(?:-|\\.)([\\w&]+)", Pattern.CASE_INSENSITIVE);
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps101 = ImmutableMap.builder();
		grps101.put(0, Release.PROP_NAME);
		grps101.put(1, Series.PROP_NAME);
		grps101.put(2, Season.PROP_NUMBER);
		grps101.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps101.put(4, Episode.PROP_TITLE);
		grps101.put(5, Release.PROP_TAGS);
		grps101.put(6, Release.PROP_GROUP);
		grps101.put(7, Subtitle.PROP_LANGUAGE);
		grps101.put(8, Subtitle.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher101 = new MappingMatcher<SimplePropDescriptor>(p101,
				grps101.build(),
				ImmutableMap.of(Series.PROP_TYPE, Series.TYPE_SEASONED));

		// The.Last.Ship.S01E06.HDTV.x264-LOL.de-SCuTV4U
		Pattern p102 = Pattern.compile("(.*?)\\.S(\\d{2})E(\\d{2})\\.(.*?)-(\\w+)\\.(de|ger|german|VO|en|english)(?:-|\\.)([\\w&]+)");
		ImmutableMap.Builder<Integer, SimplePropDescriptor> grps102 = ImmutableMap.builder();
		grps102.put(1, Series.PROP_NAME);
		grps102.put(2, Season.PROP_NUMBER);
		grps102.put(3, Episode.PROP_NUMBER_IN_SEASON);
		grps102.put(4, Release.PROP_TAGS);
		grps102.put(5, Release.PROP_GROUP);
		grps102.put(6, Subtitle.PROP_LANGUAGE);
		grps102.put(7, Subtitle.PROP_GROUP);
		MappingMatcher<SimplePropDescriptor> matcher102 = new MappingMatcher<>(p102, grps102.build(), ImmutableMap.of(Series.PROP_TYPE,
				Series.TYPE_SEASONED));

		// System.out.println(matcher102.match("The.Last.Ship.S01E06.HDTV.x264-LOL.de-SCuTV4U"));

		epiParser.setMatchers(ImmutableList.of(matcher101, matcher102));

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
