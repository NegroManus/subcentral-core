package de.subcentral.core.name;

import java.time.LocalDateTime;
import java.time.Year;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.media.MultiEpisodeHelper;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
import de.subcentral.core.metadata.release.Group;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;

public class NamingPlayground {

    public static void main(String[] args) {
        // Psych - 01x01 - Pilot.DiMENSION.English.orig.Addic7ed.com

        Series series = new Series();
        series.setName("How I Met Your Mother");
        series.setType(Series.TYPE_SEASONED);

        Season s2 = new Season(series);
        s2.setNumber(2);
        s2.setTitle("Webisodes");

        Episode epi = new Episode(series, s2);
        epi.setNumberInSeason(1);
        epi.setNumberInSeries(17);
        epi.setTitle("Weekend at Barney's");
        epi.setDate(LocalDateTime.now());

        Episode epi2 = new Episode(series);
        epi2.setNumberInSeries(18);

        Movie movie = new Movie();
        movie.setName("The Lord of the Rings");
        movie.setDate(Year.of(2002));

        MultiEpisodeHelper epis = new MultiEpisodeHelper();
        epis.getEpisodes().add(Episode.createMiniSeriesEpisode("Psych", 1));
        epis.getEpisodes().add(Episode.createMiniSeriesEpisode("Psych", 3));
        epis.getEpisodes().add(Episode.createMiniSeriesEpisode("Psych", 3));

        // Media release
        Release rel = new Release();
        rel.setName("Psych.S01E01.HDTV.XviD-LOL");
        rel.setMedia(ImmutableList.of(movie));
        rel.setGroup(Group.of("DIMENSION"));
        rel.setTags(Tags.of("720p", "HDTV", "x264"));

        // Subtitle
        Subtitle sub1 = new Subtitle();
        sub1.setMedia(epi);
        sub1.setLanguage("VO");
        sub1.setGroup(Group.of("SubCentral"));

        // Subtitle release
        SubtitleRelease subAdj = new SubtitleRelease();
        subAdj.setSingleSubtitle(sub1);
        subAdj.setTags(Tags.of("orig", "C"));
        subAdj.setSingleMatchingRelease(rel);

        System.out.println(NamingDefaults.getDefaultNamingService().name(rel));
        // long overallStart = System.nanoTime();
        // for (int i = 0; i < 1000; i++)
        // {
        // long start = System.nanoTime();
        // String name = NamingDefaults.NAMING_SERVICE.name(subRel); // ns.name(epi);//
        // double duration = TimeUtil.durationMillis(start, System.nanoTime());
        // System.out.println(name);
        // System.out.println(duration + " ms");
        // }
        // double overallDuration = TimeUtil.durationMillis(overallStart, System.nanoTime());
        // System.out.println("Overall duration: " + overallDuration + " ms");

        System.out.println(epi);
    }
}
