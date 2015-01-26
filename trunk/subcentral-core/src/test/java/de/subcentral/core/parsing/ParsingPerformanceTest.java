package de.subcentral.core.parsing;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.addic7edcom.Addic7edCom;
import de.subcentral.support.subcentralde.SubCentralDe;
import de.subcentral.support.thescene.Scene;

public class ParsingPerformanceTest
{

	public static void main(String[] args)
	{
		final ClassBasedParsingService ps = new ClassBasedParsingService("default", ImmutableSet.of(SubtitleAdjustment.class, Release.class));
		ps.registerAllParsers(Addic7edCom.getAllParsers());
		ps.registerAllParsers(SubCentralDe.getAllParsers());
		ps.registerAllParsers(Scene.getAllParsers());

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
