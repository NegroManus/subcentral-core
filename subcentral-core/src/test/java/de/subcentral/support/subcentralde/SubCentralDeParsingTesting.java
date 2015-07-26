package de.subcentral.support.subcentralde;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;

public class SubCentralDeParsingTesting
{
	@Test
	public void testEpisode01()
	{
		String name = "Game.of.Thrones.S05E01.HDTV.XviD-AFG.de-SubCentral";
		Episode epi = Episode.createSeasonedEpisode("Game.of.Thrones", 5, 1);
		Release rls = Release.create(epi, "AFG", "HDTV", "XviD");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "de", "SubCentral");
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode02()
	{
		String name = "Game.of.Thrones.S05E01.HDTV.XviD-AFG.de";
		Episode epi = Episode.createSeasonedEpisode("Game.of.Thrones", 5, 1);
		Release rls = Release.create(epi, "AFG", "HDTV", "XviD");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "de", null);
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode03()
	{
		String name = "Prison.Break.S04E18.720p.HDTV.X264-DIMENSION.de.SubCentral.V2";
		Episode epi = Episode.createSeasonedEpisode("Prison.Break", 4, 18);
		Release rls = Release.create(epi, "DIMENSION", "720p", "HDTV", "X264");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "de", "SubCentral");
		subAdj.setVersion("V2");
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode04()
	{
		String name = "Prison.Break.S04E19.HDTV.XviD-LOL.V2";
		Episode epi = Episode.createSeasonedEpisode("Prison.Break", 4, 19);
		Release rls = Release.create(epi, "LOL", "HDTV", "XviD");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, null, null);
		subAdj.setVersion("V2");
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode05()
	{
		String name = "Prison.Break.S04E20.HDTV.XviD-LOL.VO.V2";
		Episode epi = Episode.createSeasonedEpisode("Prison.Break", 4, 20);
		Release rls = Release.create(epi, "LOL", "HDTV", "XviD");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "VO", null);
		subAdj.setVersion("V2");
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode06()
	{
		String name = "Prison.Break.S04E18.HDTV.XviD-LOL.V2.de.SubCentral";
		Episode epi = Episode.createSeasonedEpisode("Prison.Break", 4, 18);
		Release rls = Release.create(epi, "LOL", "HDTV", "XviD");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "de", "SubCentral");
		subAdj.setVersion("V2");
		testParsingResult(name, subAdj);
	}

	@Test
	public void testEpisode07()
	{
		String name = "Salem.S02E04.Book.of.Shadows.1080p.WEB-DL.DD5.1.H264-ABH.de-SubCentral";
		Episode epi = Episode.createSeasonedEpisode("Salem", 2, 4, "Book.of.Shadows");
		Release rls = Release.create(epi, "ABH", "1080p", "WEB", "DL", "DD5", "1", "H264");
		SubtitleAdjustment subAdj = SubtitleAdjustment.create(rls, "de", "SubCentral");
		testParsingResult(name, subAdj);
	}

	private void testParsingResult(String name, Object expectedResult)
	{
		Object parsed = SubCentralDe.getParsingService().parse(name);
		System.out.println(parsed);
		assertEquals(expectedResult, parsed);
	}
}
