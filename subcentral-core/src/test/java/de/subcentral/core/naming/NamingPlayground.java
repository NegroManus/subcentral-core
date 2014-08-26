package de.subcentral.core.naming;

import java.time.LocalDateTime;
import java.time.Year;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.media.MultiEpisodeHelper;
import de.subcentral.core.model.media.Season;
import de.subcentral.core.model.media.Series;
import de.subcentral.core.model.release.Group;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Tag;
import de.subcentral.core.model.subtitle.Subtitle;
import de.subcentral.core.model.subtitle.SubtitleAdjustment;
import de.subcentral.core.naming.NamingStandards;

public class NamingPlayground
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

		Episode epi2 = series.newEpisode();
		epi2.setNumberInSeries(18);

		Movie movie = new Movie();
		movie.setName("The Lord of the Rings");
		movie.setDate(Year.of(2002));

		MultiEpisodeHelper epis = new MultiEpisodeHelper();
		epis.add(Episode.createMiniSeriesEpisode("Psych", 1));
		epis.add(Episode.createMiniSeriesEpisode("Psych", 3));
		epis.add(Episode.createMiniSeriesEpisode("Psych", 3));

		// Media release
		Release rel = new Release();
		rel.setName("Psych.S01E01.HDTV.XviD-LOL");
		rel.setMedia(ImmutableList.of(movie));
		rel.setGroup(new Group("DIMENSION"));
		rel.setTags(Tag.tags("720p", "HDTV", "x264"));

		// Subtitle
		Subtitle sub1 = new Subtitle();
		sub1.setMedia(epi);
		sub1.setLanguage("VO");
		sub1.setTags(Tag.tags("orig", "C"));
		sub1.setGroup(new Group("SubCentral"));

		// Subtitle release
		SubtitleAdjustment subRel = new SubtitleAdjustment();
		subRel.setSingleSubtitle(sub1);
		subRel.setSingleMatchingRelease(rel);

		System.out.println(NamingStandards.NAMING_SERVICE.name(epis));
		// long overallStart = System.nanoTime();
		// for (int i = 0; i < 1000; i++)
		// {
		// long start = System.nanoTime();
		// String name = NamingStandards.NAMING_SERVICE.name(subRel); // ns.name(epi);//
		// double duration = TimeUtil.durationMillis(start, System.nanoTime());
		// System.out.println(name);
		// System.out.println(duration + " ms");
		// }
		// double overallDuration = TimeUtil.durationMillis(overallStart, System.nanoTime());
		// System.out.println("Overall duration: " + overallDuration + " ms");

		System.out.println(epi);
	}
}
