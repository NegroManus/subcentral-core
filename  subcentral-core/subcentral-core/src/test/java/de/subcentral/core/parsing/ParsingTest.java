package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.subcentral.core.model.subtitle.SubtitleRelease;
import de.subcentral.thirdparty.com.addic7ed.Addic7edSubtitleReleaseMapper;

public class ParsingTest
{
	public static void main(String[] args)
	{
		String name = "Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com";
		// name = "Robot Chicken - 07x07 - Snarfer Image.x264-KILLERS.English.C.orig.Addic7ed.com";
		name = "24 - 09x05 - Day 9_ 3_00 PM-4_00 PM.LOL.English.C.orig.Addic7ed.com";
		name = "The Listener - 05x01 - The Wrong Man.KILLERS.English.C.orig.Addic7ed.com";

		name = "Winter's Tale (2014).DVD-Rip.Bulgarian.orig.Addic7ed.com";
		name = "the house of magic (2014).bdrip.Portuguese.orig.Addic7ed.com";
		name = "Revenge of the Bridesmaids (2010).Dvd-rip.Serbian (Latin).orig.Addic7ed.com";

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
		Map<Integer, String> groups1 = new HashMap<>();
		groups1.put(1, Addic7edSubtitleReleaseMapper.SERIES_TITLE);
		groups1.put(2, Addic7edSubtitleReleaseMapper.SEASON_NUMBER);
		groups1.put(3, Addic7edSubtitleReleaseMapper.EPISODE_NUMBER);
		groups1.put(4, Addic7edSubtitleReleaseMapper.EPISODE_TITLE);
		groups1.put(5, Addic7edSubtitleReleaseMapper.MEDIA_RELEASE_GROUP);
		groups1.put(6, Addic7edSubtitleReleaseMapper.SUBTITLE_LANGUAGE);
		groups1.put(7, Addic7edSubtitleReleaseMapper.SUBTITLE_RELEASE_TAGS);
		groups1.put(8, Addic7edSubtitleReleaseMapper.SUBTITLE_RELEASE_GROUP);

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
		Map<Integer, String> groups2 = new HashMap<>();
		groups2.put(1, Addic7edSubtitleReleaseMapper.MOVIE_NAME);
		groups2.put(2, Addic7edSubtitleReleaseMapper.MOVIE_TITLE);
		groups2.put(3, Addic7edSubtitleReleaseMapper.MOVIE_YEAR);
		groups2.put(4, Addic7edSubtitleReleaseMapper.MEDIA_RELEASE_TAGS);
		groups2.put(5, Addic7edSubtitleReleaseMapper.SUBTITLE_LANGUAGE);
		groups2.put(6, Addic7edSubtitleReleaseMapper.SUBTITLE_RELEASE_TAGS);
		groups2.put(7, Addic7edSubtitleReleaseMapper.SUBTITLE_RELEASE_GROUP);

		NumericGroupMappingMatcher matcher2 = new NumericGroupMappingMatcher(p2, groups2);

		ParsingServiceImpl ps = new ParsingServiceImpl();
		MappingServiceImpl ms = new MappingServiceImpl();
		Map<Class<?>, Mapper<?>> mappers = new HashMap<>(1);
		mappers.put(SubtitleRelease.class, new Addic7edSubtitleReleaseMapper());
		ms.setMappers(mappers);

		ps.setMappingService(ms);

		Map<MappingMatcher, Class<?>> matchers = new HashMap<>(1);
		matchers.put(matcher1, SubtitleRelease.class);
		matchers.put(matcher2, SubtitleRelease.class);
		ps.setMatchers(matchers);

		Object obj = ps.parse(name);

		System.out.println(obj);
	}
}
