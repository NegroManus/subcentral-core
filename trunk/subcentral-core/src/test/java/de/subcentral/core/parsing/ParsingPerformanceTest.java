package de.subcentral.core.parsing;

import com.google.common.collect.ImmutableListMultimap;

import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7ed.Addic7ed;
import de.subcentral.support.scene.Scene;
import de.subcentral.support.subcentral.SubCentral;

public class ParsingPerformanceTest
{

	public static void main(String[] args)
	{
		final SimpleParsingService ps = new SimpleParsingService("default");
		ImmutableListMultimap.Builder<Class<?>, Parser<?>> parsers = ImmutableListMultimap.builder();
		parsers.putAll(Addic7ed.getAllParsers());
		parsers.putAll(SubCentral.getAllParsers());
		parsers.putAll(Scene.getAllParsers());
		ps.setParsers(parsers.build());

		String name = "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com";
		String name2 = "The.Blacklist.S02E05.720p.HDTV.X264-DIMENSION";
		String name3 = "The.Blacklist.S02E05E06.720p.HDTV.x264-DIMENSION.de-SubCentral";

		long start = System.nanoTime();
		int times = 100_000;
		for (int i = 0; i < times; i++)
		{
			Object o = ps.parse(name);
			Object o2 = ps.parse(name2);
			Object o3 = ps.parse(name3);
		}
		double duration = TimeUtil.durationMillis(start);
		double durationPerTime = duration / times / 3;
		System.out.println("duration: " + duration);
		System.out.println("durationPerTime: " + durationPerTime);
	}

}
