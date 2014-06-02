package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Medias;
import de.subcentral.core.release.Group;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Tag;
import de.subcentral.core.subtitle.Subtitle;
import de.subcentral.core.subtitle.SubtitleRelease;
import de.subcentral.core.util.TimeUtil;

public class NamingTest {

	public static void main(String[] args) {
		// Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com

		NamingServiceImpl ns = new NamingServiceImpl();
		Map<Class<?>, Namer<?>> namers = new HashMap<>(3);
		namers.put(Episode.class, new Addic7edEpisodeNamer());
		namers.put(MediaRelease.class, new Addic7edMediaReleaseNamer());
		namers.put(SubtitleRelease.class, new Addic7edSubtitleReleaseNamer());
		ns.setNamers(namers);

		Episode epi = Medias.newEpisode("Psych", 1, 1, "Pilot");

		// Media release
		MediaRelease rel = new MediaRelease();
		// rel.setExplicitName("Psych.S01E01.HDTV.XviD-LOL");
		rel.setMaterial(epi);
		rel.setGroup(new Group("DiMENSION"));
		rel.setTags(ImmutableList.of(new Tag("720p")));

		// Subtitle
		Subtitle sub1 = new Subtitle();
		sub1.setMedia(epi);
		sub1.setLanguage("English");

		// Subtitle release
		SubtitleRelease subRel = new SubtitleRelease();
		subRel.setMaterial(sub1);
		subRel.setCompatibleMediaRelease(rel);
		subRel.setTags(ImmutableList.of(new Tag("orig")));

		long overallStart = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			epi.setNumberInSeason(i);
			long start = System.nanoTime();
			String name = ns.name(subRel);
			double duration = TimeUtil.durationMillis(start, System.nanoTime());
			System.out.println(name);
			System.out.println(duration + " ms");
		}
		double overallDuration = TimeUtil.durationMillis(overallStart,
				System.nanoTime());
		System.out.println("Overall duration: " + overallDuration + " ms");
	}
}
