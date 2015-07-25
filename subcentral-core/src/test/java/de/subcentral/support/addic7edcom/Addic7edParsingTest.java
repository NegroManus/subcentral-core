package de.subcentral.support.addic7edcom;

import java.time.Year;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.Tag;
import de.subcentral.core.metadata.subtitle.Subtitle;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.core.standardizing.StandardizingChange;
import de.subcentral.core.standardizing.StandardizingDefaults;

public class Addic7edParsingTest
{
    // name = "Robot Chicken - 07x07 - Snarfer Image.x264-KILLERS.English.C.orig.Addic7ed.com";
    // name = "24 - 09x05 - Day 9_ 3_00 PM-4_00 PM.LOL.English.C.orig.Addic7ed.com";
    // name = "The Listener - 05x01 - The Wrong Man.KILLERS.English.C.orig.Addic7ed.com";
    //
    // name = "Winter's Tale (2014).DVD-Rip.Bulgarian.orig.Addic7ed.com";
    // name = "the house of magic (2014).bdrip.Portuguese.orig.Addic7ed.com";
    // name = "Revenge of the Bridesmaids (2010).Dvd-rip.Serbian (Latin).orig.Addic7ed.com";
    @Test
    public void testEpisode01()
    {
	String name = "Ben 10_ Omniverse - 01x26 - The Frogs of War, Part 1.WEB-DL.x264.AAC.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Ben 10_ Omniverse", 1, 26, "The Frogs of War, Part 1");
	Release rls = Release.create(epi, null, "WEB-DL", "x264", "AAC");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode01", adj, name);
    }

    @Test
    public void testEpisode02()
    {
	String name = "Psych - 07x02 - Juliet Takes a Luvvah.EVOLVE.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Psych", 7, 2, "Juliet Takes a Luvvah");
	Release rls = Release.create(epi, "EVOLVE");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode02", adj, name);
    }

    @Test
    public void testEpisode03()
    {
	String name = "10 Things I Hate About You - 01x01 - Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot");
	Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setHearingImpaired(true);

	compare("testEpisode03", adj, name);
    }

    @Test
    public void testEpisode04()
    {
	String name = "Switched At Birth - 03x04 - It Hurts to Wait With Love If Love Is Somewhere Else.HDTV.x264-EXCELLENCE.Dutch.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Switched At Birth", 3, 4, "It Hurts to Wait With Love If Love Is Somewhere Else");
	Release rls = Release.create(epi, "EXCELLENCE", "HDTV", "x264");
	Subtitle sub = new Subtitle(epi, "Dutch");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("orig"));

	compare("testEpisode04", adj, name);
    }

    @Test
    public void testEpisode05()
    {
	String name = "Vikings - 01x08 - Sacrifice.x264.2HD.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Vikings", 1, 8, "Sacrifice");
	Release rls = Release.create(epi, "2HD", "x264");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode05", adj, name);
    }

    @Test
    public void testEpisode06()
    {
	String name = "Out There (2013) - 01x09 - Viking Days.480p.WEB-DL.x264-mSD.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Out There (2013)", "Out There", 1, 9, "Viking Days");
	epi.getSeries().setDate(Year.of(2013));
	Release rls = Release.create(epi, "mSD", "480p", "WEB-DL", "x264");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode06", adj, name);
    }

    @Test
    public void testEpisode07()
    {
	String name = "Psych - 01x01 - Pilot.DVDRip TOPAZ.French.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Psych", 1, 1, "Pilot");
	Release rls = Release.create(epi, "TOPAZ", "DVDRip");
	Subtitle sub = new Subtitle(epi, "French");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("orig"));

	compare("testEpisode07", adj, name);
    }

    @Test
    public void testEpisode08()
    {
	String name = "Psych - 05x04 - Chivalry Is Not Dead...But Someone Is.FQM.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Psych", 5, 4, "Chivalry Is Not Dead...But Someone Is");
	Release rls = Release.create(epi, "FQM");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode08", adj, name);
    }

    @Test
    public void testEpisode09()
    {
	String name = "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.English.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Dallas (2012)", "Dallas", 2, 8, "J.R.'s Masterpiece");
	epi.getSeries().setDate(Year.of(2012));
	Release rls = Release.create(epi, "LOL");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("orig"));

	compare("testEpisode09", adj, name);
    }

    @Test
    public void testEpisode10()
    {
	String name = "Dallas (2012) - 02x08 - J.R.'s Masterpiece.LOL.German.C.updated.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Dallas (2012)", "Dallas", 2, 8, "J.R.'s Masterpiece");
	epi.getSeries().setDate(Year.of(2012));
	Release rls = Release.create(epi, "LOL");
	Subtitle sub = new Subtitle(epi, "German");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "updated"));

	compare("testEpisode10", adj, name);
    }

    @Test
    public void testEpisode11()
    {
	String name = "Psych - 07x03 - Lassie Jerky.WEB-DL.English.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Psych", 7, 3, "Lassie Jerky");
	Release rls = Release.create(epi, null, "WEB-DL");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("orig"));

	compare("testEpisode11", adj, name);
    }

    @Test
    public void testEpisode12()
    {
	String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264-DIMENSION.English.HI.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
	Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setHearingImpaired(true);

	compare("testEpisode12", adj, name);
    }

    @Test
    public void testEpisode13()
    {
	String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.HDTV.x264.DIMENSION.English.HI.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
	Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "x264");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setHearingImpaired(true);

	compare("testEpisode13", adj, name);
    }

    @Test
    public void testEpisode14()
    {
	String name = "10 Things I Hate About You - 01x01 - Pilot... And Another Pilot.720p.WEB-DL.English.HI.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("10 Things I Hate About You", 1, 1, "Pilot... And Another Pilot");
	Release rls = Release.create(epi, null, "720p", "WEB-DL");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setHearingImpaired(true);

	compare("testEpisode14", adj, name);
    }

    @Test
    public void testEpisode15()
    {
	String name = "The Office (US) - 02x16 - Valentine's day.DVDRip TOPAZ.French.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("The Office (US)", "The Office", 2, 16, "Valentine's day");
	epi.getSeries().getCountries().add("US");
	Release rls = Release.create(epi, "TOPAZ", "DVDRip");
	Subtitle sub = new Subtitle(epi, "French");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("orig"));

	compare("testEpisode15", adj, name);
    }

    @Test
    public void testEpisode16()
    {
	String name = "Finding Carter - 01x07 - Throw Momma From the Train.KILLERS, MSD.English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Finding Carter", 1, 7, "Throw Momma From the Train");
	Release rls1 = Release.create(epi, "KILLERS");
	Release rls2 = Release.create(epi, "MSD");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, ImmutableSet.of(rls1, rls2));
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode16", adj, name);
    }
    
    @Test
    public void testEpisode17()
    {
	String name = "Death in Paradise - 04x04 - Series 4, Episode 4.FoV (HDTV + 720p).English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("Death in Paradise", 4, 4, "Series 4, Episode 4");
	Release rls = Release.create(epi, "FoV", "HDTV", "720p");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode17", adj, name);
    }
    
    @Test
    public void testEpisode18()
    {
	String name = "The Saboteurs (aka The Heavy Water War) - 01x06 - Episode 6 (Finale).TVC (HDTV).English.C.orig.Addic7ed.com";

	Episode epi = Episode.createSeasonedEpisode("The Saboteurs (aka The Heavy Water War)", 1, 6, "Episode 6 (Finale)");
	Release rls = Release.create(epi, "TVC", "HDTV");
	Subtitle sub = new Subtitle(epi, "English");
	sub.setSource("Addic7ed.com");
	SubtitleAdjustment adj = new SubtitleAdjustment(name, sub, rls);
	adj.setTags(Tag.list("C", "orig"));

	compare("testEpisode18", adj, name);
    }


    private static final void compare(String testName, SubtitleAdjustment expected, String nameToParse)
    {
	Object parsed = Addic7edCom.getParsingService().parse(nameToParse);
	List<StandardizingChange> changes = StandardizingDefaults.getDefaultStandardizingService().standardize(parsed);
	changes.stream().forEach(c -> System.out.println(c));

	System.out.println("Results for test: " + testName);
	System.out.println("Expected: " + expected);
	System.out.println("Parsed  : " + parsed);
	Assert.assertEquals(expected.toString(), parsed.toString());
    }
}
