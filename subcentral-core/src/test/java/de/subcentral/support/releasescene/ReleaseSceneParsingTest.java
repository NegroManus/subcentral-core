package de.subcentral.support.releasescene;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;

public class ReleaseSceneParsingTest
{

	@Test
	public void testEpisode01()
	{
		String name = "Penn.Zero.Part-Time.Hero.S01E08.HDTV.x264-W4F";
		Episode epi = Episode.createSeasonedEpisode("Penn.Zero.Part-Time.Hero", 1, 8);
		Release rls = Release.create(epi, "W4F", "HDTV", "x264");
		assertParsingResult(name, rls);
	}

	@Test
	public void testEpisode02()
	{
		String name = "Penn.Zero.Part-Time.Hero.S01E08.720p.HDTV.x264-W4F";
		Episode epi = Episode.createSeasonedEpisode("Penn.Zero.Part-Time.Hero", 1, 8);
		Release rls = Release.create(epi, "W4F", "720p", "HDTV", "x264");
		assertParsingResult(name, rls);
	}

	@Test
	public void testEpisode03()
	{
		String name = "Penn.Zero.Part-Time.Hero.S01E08.1080p.WEBRip.AAC2.0.x264";
		Episode epi = Episode.createSeasonedEpisode("Penn.Zero.Part-Time.Hero", 1, 8);
		Release rls = Release.create(epi, null, "1080p", "WEBRip", "AAC2", "0", "x264");
		assertParsingResult(name, rls);
	}

	@Test
	public void testEpisode04()
	{
		String name = "Penn.Zero.Part-Time.Hero.S01E08.1080p.WEBRip.AAC2.0.x264-Ntb";
		Episode epi = Episode.createSeasonedEpisode("Penn.Zero.Part-Time.Hero", 1, 8);
		Release rls = Release.create(epi, "Ntb", "1080p", "WEBRip", "AAC2", "0", "x264");
		assertParsingResult(name, rls);
	}

	private void assertParsingResult(String name, Object expectedResult)
	{
		Object parsed = ReleaseScene.getParsingService().parse(name);
		System.out.println(parsed);
		assertEquals(expectedResult, parsed);
	}

}
