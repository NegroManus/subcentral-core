package de.subcentral.core.naming;

import java.time.LocalDateTime;
import java.time.Year;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Movie;
import de.subcentral.core.media.MultiEpisode;
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

		Series series = new Series();
		series.setName("How I Met Your Mother");
		series.setType(Series.TYPE_SEASONED);

		Season s2 = series.newSeason();
		s2.setNumber(2);
		s2.setTitle("Webisodes");

		Episode epi = series.newEpisode(s2);
		epi.setNumberInSeason(1);
		epi.setNumberInSeries(17);
		epi.setTitle("Weekend at Barney's");
		epi.setDate(LocalDateTime.now());

		Movie movie = new Movie();
		movie.setTitle("The Lord of the Rings");
		movie.setDate(Year.of(2002));

		MultiEpisode epis = new MultiEpisode();
		Episode epi2 = series.newEpisode();
		epi2.setNumberInSeries(18);

		epis.add(Episode.newMiniSeriesEpisode("Psych", 1));
		epis.add(Episode.newMiniSeriesEpisode("Psych", 2));

		// Media release
		MediaRelease rel = new MediaRelease();
		// rel.setExplicitName("Psych.S01E01.HDTV.XviD-LOL");
		rel.setMaterial(epi);
		rel.setGroup(new Group("DIMENSION"));
		rel.setTags(Releases.tags("720p", "HDTV", "x264"));

		// Subtitle
		Subtitle sub1 = new Subtitle();
		sub1.setMediaItem(epi);
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
			System.out.println(NamingStandards.NAMING_SERVICE.name(epis));
			long start = System.nanoTime();
			String name = NamingStandards.NAMING_SERVICE.name(subRel); // ns.name(epi);//
			double duration = TimeUtil.durationMillis(start, System.nanoTime());
			System.out.println(name);
			System.out.println(duration + " ms");
		}
		double overallDuration = TimeUtil.durationMillis(overallStart, System.nanoTime());
		System.out.println("Overall duration: " + overallDuration + " ms");

		System.out.println(epi);
	}
}
