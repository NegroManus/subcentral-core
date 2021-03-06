package de.subcentral.support.addic7edcom;

import java.time.Year;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import de.subcentral.core.correct.Correction;
import de.subcentral.core.correct.CorrectionDefaults;
import de.subcentral.core.metadata.Site;
import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Movie;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tags;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;

public class Addic7edParsingTest {

    private static final Site ADDIC7ED_SITE_WITH_ONLY_NAME = new Site(Addic7edCom.getSite().getName());

    @Test
    public void testEpisode01() {
        String name = "Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Ben 10_ Omniverse", 1, 26, "The Frogs of War, Part 1");
        Release rls = Release.create(epi, null, "WEB-DL", "x264", "AAC");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode01", subRls, name);
    }

    @Test
    public void testEpisode02() {
        String name = "Psych - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Psych", 7, 2, "Juliet Takes a Luvvah");
        Release rls = Release.create(epi, "EVOLVE");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode02", subRls, name);
    }

    @Test
    public void testEpisode03() {
        String name = "10 Things I Hate About You - 01x01 - Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot");
        Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, ImmutableList.of(SubtitleRelease.HEARING_IMPAIRED_TAG));

        compare("testEpisode03", subRls, name);
    }

    @Test
    public void testEpisode04() {
        String name = "Switched At Birth - 03x04 - It Hurts to Wait With Love If Love Is Somewhere Else.HDTV.x264-EXCELLENCE.Dutch.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Switched At Birth", 3, 4, "It Hurts to Wait With Love If Love Is Somewhere Else");
        Release rls = Release.create(epi, "EXCELLENCE", "HDTV", "x264");
        Subtitle sub = new Subtitle(epi, "Dutch");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testEpisode04", subRls, name);
    }

    @Test
    public void testEpisode05() {
        String name = "Vikings - 01x08 - Sacrifice.x264.2HD.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Vikings", 1, 8, "Sacrifice");
        Release rls = Release.create(epi, "2HD", "x264");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode05", subRls, name);
    }

    @Test
    public void testEpisode06() {
        String name = "Out There (2013) - 01x09 - Viking Days.480p.WEB-DL.x264-mSD.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Out There (2013)", "Out There", 1, 9, "Viking Days");
        epi.getSeries().setDate(Year.of(2013));
        Release rls = Release.create(epi, "mSD", "480p", "WEB-DL", "x264");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode06", subRls, name);
    }

    @Test
    public void testEpisode07() {
        String name = "Psych - 01x01 - Pilot.DVDRip TOPAZ.French.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Psych", 1, 1, "Pilot");
        Release rls = Release.create(epi, "TOPAZ", "DVDRip");
        Subtitle sub = new Subtitle(epi, "French");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testEpisode07", subRls, name);
    }

    @Test
    public void testEpisode08() {
        String name = "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Psych", 5, 4, "Chivalry Is Not Dead...But Someone Is");
        Release rls = Release.create(epi, "FQM");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode08", subRls, name);
    }

    @Test
    public void testEpisode09() {
        String name = "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Dallas (2012)", "Dallas", 2, 8, "J.R.'s Masterpiece");
        epi.getSeries().setDate(Year.of(2012));
        Release rls = Release.create(epi, "LOL");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testEpisode09", subRls, name);
    }

    @Test
    public void testEpisode10() {
        String name = "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Dallas (2012)", "Dallas", 2, 8, "J.R.'s Masterpiece");
        epi.getSeries().setDate(Year.of(2012));
        Release rls = Release.create(epi, "LOL");
        Subtitle sub = new Subtitle(epi, "German");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "updated"));

        compare("testEpisode10", subRls, name);
    }

    @Test
    public void testEpisode11() {
        String name = "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Psych", 7, 3, "Lassie Jerky");
        Release rls = Release.create(epi, null, "WEB-DL");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testEpisode11", subRls, name);
    }

    @Test
    public void testEpisode12() {
        String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
        Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, ImmutableList.of(SubtitleRelease.HEARING_IMPAIRED_TAG));

        compare("testEpisode12", subRls, name);
    }

    @Test
    public void testEpisode13() {
        String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264.DIMENSION.English.HI.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
        Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, ImmutableList.of(SubtitleRelease.HEARING_IMPAIRED_TAG));

        compare("testEpisode13", subRls, name);
    }

    @Test
    public void testEpisode14() {
        String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
        Release rls = Release.create(epi, null, "720p", "WEB-DL");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls);
        subRls.setHearingImpaired(true);

        compare("testEpisode14", subRls, name);
    }

    @Test
    public void testEpisode15() {
        String name = "The Office (US) - 02x16 - Valentine's day.DVDRip TOPAZ.French.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("The Office (US)", "The Office", 2, 16, "Valentine's day");
        epi.getSeries().getCountries().add("US");
        Release rls = Release.create(epi, "TOPAZ", "DVDRip");
        Subtitle sub = new Subtitle(epi, "French");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testEpisode15", subRls, name);
    }

    @Test
    public void testEpisode16() {
        String name = "Finding Carter - 01x07 - Throw Momma From the Train.KILLERS, MSD.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Finding Carter", 1, 7, "Throw Momma From the Train");
        Release rls1 = Release.create(epi, "KILLERS");
        Release rls2 = Release.create(epi, "MSD");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, ImmutableSet.of(rls1, rls2), Tags.of("C", "orig"));

        compare("testEpisode16", subRls, name);
    }

    @Test
    public void testEpisode17() {
        String name = "Death in Paradise - 04x04 - Series 4, Episode 4.FoV (HDTV + 720p).English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Death in Paradise", 4, 4, "Series 4, Episode 4");
        Release rls = Release.create(epi, "FoV", "HDTV", "720p");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode17", subRls, name);
    }

    @Test
    public void testEpisode18() {
        String name = "The Saboteurs (aka The Heavy Water War) - 01x06 - Episode 6 (Finale).TVC (HDTV).English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("The Saboteurs (aka The Heavy Water War)", 1, 6, "Episode 6 (Finale)");
        Release rls = Release.create(epi, "TVC", "HDTV");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode18", subRls, name);
    }

    @Test
    public void testEpisode19() {
        String name = "Hannibal - 03x10 - ...And the Woman Clothed in Sun.WEB-DL.English.HI.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Hannibal", 3, 10, "...And the Woman Clothed in Sun");
        Release rls = Release.create(epi, null, "WEB-DL");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("HI", "C", "orig"));

        compare("testEpisode19", subRls, name);
    }

    @Test
    public void testEpisode20() {
        String name = "Review with Forrest MacNeil - 02x04 - Cult; Perfect Body.WEBRip.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("Review with Forrest MacNeil", 2, 04, "Cult; Perfect Body");
        Release rls = Release.create(epi, null, "WEBRip");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode20", subRls, name);
    }

    @Test
    public void testEpisode21() {
        String name = "From Dusk Till Dawn_ The Series - 01x01 - Pilot.Webrip.2HD.English.C.orig.Addic7ed.com";

        Episode epi = Episode.createSeasonedEpisode("From Dusk Till Dawn_ The Series", 1, 1, "Pilot");
        Release rls = Release.create(epi, "2HD", "Webrip");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode21", subRls, name);
    }

    @Test
    public void testEpisode22() {
        String name = "CSI_ Cyber - 02x05 - hack E.R..DIMENSION.English.C.orig.Addic7ed.com";
        Episode epi = Episode.createSeasonedEpisode("CSI_ Cyber", 2, 5, "hack E.R.");
        Release rls = Release.create(epi, "DIMENSION");
        Subtitle sub = new Subtitle(epi, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testEpisode22", subRls, name);
    }

    @Test
    public void testMovie01() {
        String name = "The Man Behind the Throne (2013).CBFM.English.C.orig.Addic7ed.com";
        Movie mov = new Movie("The Man Behind the Throne", Year.of(2013));
        Release rls = Release.create(mov, "CBFM");
        Subtitle sub = new Subtitle(mov, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("C", "orig"));

        compare("testMovie01", subRls, name);
    }

    @Test
    public void testMovie02() {
        String name = "Winter's Tale (2014).DVD-Rip.English.orig.Addic7ed.com";
        Movie mov = new Movie("Winter's Tale", Year.of(2014));
        Release rls = Release.create(mov, null, "DVD-Rip");
        Subtitle sub = new Subtitle(mov, "English");
        sub.setSource(ADDIC7ED_SITE_WITH_ONLY_NAME);
        SubtitleRelease subRls = new SubtitleRelease(name, sub, rls, Tags.of("orig"));

        compare("testMovie02", subRls, name);
    }

    private static final void compare(String testMethodName, SubtitleRelease expected, String nameToParse) {
        Object parsed = Addic7edCom.getParsingService().parse(nameToParse);
        List<Correction> changes = CorrectionDefaults.getDefaultCorrectionService().correct(parsed);
        changes.stream().forEach(c -> System.out.println(c));

        System.out.println("Results for test: " + testMethodName);
        System.out.println("Expected: " + expected);
        System.out.println("Parsed  : " + parsed);
        Assert.assertEquals(expected.toString(), parsed.toString());
    }
}
