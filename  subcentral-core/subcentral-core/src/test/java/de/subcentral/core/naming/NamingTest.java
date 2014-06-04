package de.subcentral.core.naming;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.impl.com.addic7ed.Addic7edEpisodeNamer;
import de.subcentral.core.impl.com.addic7ed.Addic7edMediaReleaseNamer;
import de.subcentral.core.impl.com.addic7ed.Addic7edSubtitleReleaseNamer;
import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Season;
import de.subcentral.core.media.Series;
import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Releases;
import de.subcentral.core.release.Tag;
import de.subcentral.core.subtitle.Subtitle;
import de.subcentral.core.subtitle.SubtitleRelease;
import de.subcentral.core.util.TimeUtil;

public class NamingTest
{

	public static void main(String[] args)
	{
		// Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com

		NamingServiceImpl ns = new NamingServiceImpl();
		Map<Class<?>, Namer<?>> namers = new HashMap<>(3);
		namers.put(Episode.class, new Addic7edEpisodeNamer());
		namers.put(MediaRelease.class, new Addic7edMediaReleaseNamer());
		namers.put(SubtitleRelease.class, new Addic7edSubtitleReleaseNamer());
		ns.setNamers(namers);

		Series psych = new Series();
		psych.setTitle("How I Met Your Mother");
		psych.setType(Series.TYPE_SERIES);

		Season s2 = psych.addSeason();
		s2.setNumber(2);
		s2.setTitle("Webisodes");

		Episode epi = psych.addEpisode(s2);
		epi.setNumberInSeason(1);
		epi.setNumberInSeries(17);
		epi.setTitle("Weekend at Barney's");
		epi.setDate(LocalDateTime.now());

		// Media release
		MediaRelease rel = new MediaRelease();
		// rel.setExplicitName("Psych.S01E01.HDTV.XviD-LOL");
		rel.setMaterial(epi);
		rel.setGroup(new Group("DIMENSION"));
		rel.setTags(Releases.tagsOf("720p", "HDTV", "x264"));

		// Subtitle
		Subtitle sub1 = new Subtitle();
		sub1.setMedia(epi);
		sub1.setLanguage("VO");

		// Subtitle release
		SubtitleRelease subRel = new SubtitleRelease();
		subRel.setMaterial(sub1);
		subRel.setCompatibleMediaRelease(rel);
		subRel.setTags(ImmutableList.of(new Tag("orig")));
		subRel.setGroup(new Group("SubCentral"));

		long overallStart = System.nanoTime();
		for (int i = 0; i < 1000; i++)
		{
			long start = System.nanoTime();
			String name = NamingStandards.NAMING_SERVICE.name(subRel); // ns.name(epi);//
			double duration = TimeUtil.durationMillis(start, System.nanoTime());
			System.out.println(name);
			System.out.println(duration + " ms");
		}
		double overallDuration = TimeUtil.durationMillis(overallStart, System.nanoTime());
		System.out.println("Overall duration: " + overallDuration + " ms");
	}
}
