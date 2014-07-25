package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingStandards;
import de.subcentral.core.util.SimplePropDescriptor;
import de.subcentral.impl.addic7ed.Addic7edSubtitleAdjustmentMapper;

public class ParsingTest
{
	public static void main(String[] args)
	{
		String name = "Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com";
		// name = "Robot Chicken - 07x07 - Snarfer Image.x264-KILLERS.English.C.orig.Addic7ed.com";
		// name = "24 - 09x05 - Day 9_ 3_00 PM-4_00 PM.LOL.English.C.orig.Addic7ed.com";
		// name = "The Listener - 05x01 - The Wrong Man.KILLERS.English.C.orig.Addic7ed.com";
		//
		// name = "Winter's Tale (2014).DVD-Rip.Bulgarian.orig.Addic7ed.com";
		// name = "the house of magic (2014).bdrip.Portuguese.orig.Addic7ed.com";
		// name = "Revenge of the Bridesmaids (2010).Dvd-rip.Serbian (Latin).orig.Addic7ed.com";

		// Episode matcher
		Pattern p1 = Pattern.compile("(.*?) - (\\d{2})x(\\d{2}) - ([^\\.]+)\\.([\\w-]+)\\.(English)\\.([\\w\\.]+)\\.(Addic7ed.com)");
		//
		Matcher m = p1.matcher(name);
		if (m.matches())
		{
			System.out.println("match");
			for (int i = 0; i < m.groupCount() + 1; i++)
			{
				System.out.println(i + "=" + m.group(i));
			}
		}
		Map<Integer, SimplePropDescriptor> groups1 = new HashMap<>();
		groups1.put(1, Series.PROP_NAME);
		groups1.put(2, Season.PROP_NUMBER);
		groups1.put(3, Episode.PROP_NUMBER_IN_SEASON);
		groups1.put(4, Episode.PROP_TITLE);
		groups1.put(5, Release.PROP_GROUP);
		groups1.put(6, Subtitle.PROP_LANGUAGE);
		groups1.put(7, Subtitle.PROP_TAGS);
		groups1.put(8, Subtitle.PROP_SOURCE);
		NumericGroupMappingMatcher matcher1 = new NumericGroupMappingMatcher(p1, groups1);

		// Movie matcher
		Pattern p2 = Pattern.compile("((.*?) \\((\\d{4})\\))\\.([\\w-]+)\\.([\\w \\(\\)]+)\\.([\\w\\.]+)\\.(Addic7ed.com)");
		//
		Matcher m2 = p2.matcher(name);
		if (m2.matches())
		{
			System.out.println("match");
			for (int i = 0; i < m2.groupCount() + 1; i++)
			{
				System.out.println(i + "=" + m2.group(i));
			}
		}
		Map<Integer, SimplePropDescriptor> groups2 = new HashMap<>();
		groups2.put(1, Movie.PROP_TITLE);
		groups2.put(2, Movie.PROP_NAME);
		groups2.put(3, Movie.PROP_DATE);
		groups2.put(4, Release.PROP_GROUP);
		groups2.put(5, Subtitle.PROP_LANGUAGE);
		groups2.put(6, Subtitle.PROP_TAGS);
		groups2.put(7, Subtitle.PROP_SOURCE);

		NumericGroupMappingMatcher matcher2 = new NumericGroupMappingMatcher(p2, groups2);

		ParsingServiceImpl ps = new ParsingServiceImpl();
		MappingServiceImpl ms = new MappingServiceImpl();
		Map<Class<?>, Mapper<?>> mappers = new HashMap<>(1);
		mappers.put(SubtitleAdjustment.class, new Addic7edSubtitleAdjustmentMapper());
		ms.setMappers(mappers);

		ps.setMappingService(ms);
		ps.registerMatcher(SubtitleAdjustment.class, matcher1);
		ps.registerMatcher(SubtitleAdjustment.class, matcher2);

		Object obj = ps.parse(name);

		System.out.println(obj);
		System.out.println(NamingStandards.NAMING_SERVICE.name(obj));
	}
}
