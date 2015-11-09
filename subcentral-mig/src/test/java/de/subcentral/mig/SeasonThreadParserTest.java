package de.subcentral.mig;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.junit.Test;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleFile;
import de.subcentral.mig.SeasonThreadParser.SeasonThreadContent;

public class SeasonThreadParserTest
{
	private SeasonThreadParser parser = new SeasonThreadParser();

	@Test
	public void testParseAtlantis2013S02() throws IOException
	{
		parse("thread-atlantis2013_s02.html");
	}

	@Test
	public void testParseCastleS08Incomplete() throws IOException
	{
		parse("thread-castle_s08_incomplete.html");
	}

	@Test
	public void testParseDontTrustTheBitchS02() throws IOException
	{
		parse("thread-donttrustthebitch_s02.html");
	}

	@Test
	public void testParseGameOfThronesS04() throws IOException
	{
		parse("thread-gameofthrones_s04.html");
	}

	@Test
	public void testParseHowIMetYourMotherS09() throws IOException
	{
		parse("thread-howimetyourmother_s09.html");
	}

	@Test
	public void testParseLostS06() throws IOException
	{
		parse("thread-lost_s06.html");
	}

	@Test
	public void testParseMrRobotS01() throws IOException
	{
		parse("thread-mrrobot_s01.html");
	}

	@Test
	public void testParseOnceUponATimeS03() throws IOException
	{
		parse("thread-onceuponatime_s03.html");
	}

	@Test
	public void testParseOnceUponATimeS04() throws IOException
	{
		parse("thread-onceuponatime_s04.html");
	}

	@Test
	public void testParsePsychS07() throws IOException
	{
		parse("thread-psych_s07.html");
	}

	@Test
	public void testParsePsychS08() throws IOException
	{
		parse("thread-psych_s08.html");
	}

	@Test
	public void testParseTheMentalistS01() throws IOException
	{
		parse("thread-thementalist_s01.html");
	}

	private void parse(String resourceFilename) throws IOException
	{
		Document doc = MigTestUtil.parseDoc(getClass(), resourceFilename);
		SeasonThreadContent data = parser.parse(doc);
		printSeasonThreadContent(data);
	}

	private void printSeasonThreadContent(SeasonThreadContent content)
	{
		System.out.println();
		System.out.println("Seasons:");
		for (Season season : content.getSeasons())
		{
			System.out.println(season);
			System.out.println();
			System.out.println("Episodes:");
			for (Episode epi : season.getEpisodes())
			{
				System.out.println(epi);
			}
		}

		System.out.println();
		System.out.println("Subs:");
		for (SubtitleFile subAdj : content.getSubtitleAdjustments())
		{
			System.out.println(subAdj);
		}
	}

}
