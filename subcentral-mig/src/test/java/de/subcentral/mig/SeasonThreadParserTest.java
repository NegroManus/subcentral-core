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
		Document doc = MigTestUtil.parseDoc(getClass(), "thread-mrrobot_s01.html");
		SeasonThreadData data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	@Test
	public void testParseAtlantis2013S02() throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), "thread-atlantis2013_s02.html");
		SeasonThreadData data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	@Test
	public void testCastleS08Incomplete() throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), "thread-castle_s08_incomplete.html");
		SeasonThreadData data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	@Test
	public void testHimymS09() throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), "thread-himym_s09.html");
		SeasonThreadData data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	@Test
	public void testGotS04() throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), "thread-got_s04.html");
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
