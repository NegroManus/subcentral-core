package de.subcentral.mig;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleAdjustment;
import de.subcentral.mig.SeasonThreadParser.SeasonThreadData;

public class SeasonThreadParserTest
{
	private SeasonThreadParser parser = new SeasonThreadParser();

	@Test
	public void testParseMrRobotS01() throws IOException
	{
		parse("thread-mrrobot_s01.html");
	}

	@Test
	public void testParseAtlantis2013S02() throws IOException
	{
		parse("thread-atlantis2013_s02.html");
	}

	@Test
	public void testCastleS08Incomplete() throws IOException
	{
		parse("thread-castle_s08_incomplete.html");
	}

	@Test
	public void testHimymS09() throws IOException
	{
		parse("thread-himym_s09.html");
	}

	@Test
	public void testGotS04() throws IOException
	{
		parse("thread-got_s04.html");
	}

	@Test
	public void testDontTrustTheBitchS02() throws IOException
	{
		parse("thread-donttrustthebitch_s02.html");
	}

	@Test
	public void testPsychS07() throws IOException
	{
		parse("thread-psych_s07.html");
	}

	@Test
	public void testPsychS08() throws IOException
	{
		parse("thread-psych_s08.html");
	}

	private void parse(String resourceFilename) throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), resourceFilename);
		SeasonThreadData data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	private void printSeasonThreadContent(SeasonThreadData content)
	{
		System.out.println("Seasons:");
		for (Season season : content.getSeasons())
		{
			System.out.println(season);
			System.out.println("Episodes:");
			for (Episode epi : season.getEpisodes())
			{
				System.out.println(epi);
			}
		}

		System.out.println("Subs:");
		for (SubtitleAdjustment subAdj : content.getSubtitleAdjustments())
		{
			System.out.println(subAdj);
		}
	}

}
