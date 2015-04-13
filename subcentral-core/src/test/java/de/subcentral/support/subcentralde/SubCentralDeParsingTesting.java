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

	private void testParsingResult(String name, Object expectedResult)
	{
		Object parsed = SubCentralDe.getParsingService().parse(name);
		System.out.println(parsed);
		assertEquals(expectedResult, parsed);
	}
}
