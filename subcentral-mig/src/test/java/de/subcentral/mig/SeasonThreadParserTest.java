package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleFile;
import de.subcentral.mig.process.SeasonThreadParser;
import de.subcentral.mig.process.SeasonThreadParser.SeasonThreadContent;

public class SeasonThreadParserTest
{
	private SeasonThreadParser parser = new SeasonThreadParser();

	@Test
	public void testParsePsychS05() throws IOException
	{
		parse("Psych - Staffel 5 - [DE-Subs: 16 | VO-Subs: 16] - [Komplett]", "post-psych_s05.html");
	}

	private void parse(String postTitle, String postContentRresourceFilename) throws IOException
	{
		String content = Resources.toString(Resources.getResource(SeasonThreadParserTest.class, postContentRresourceFilename), StandardCharsets.UTF_8);
		SeasonThreadContent data = parser.parse(postTitle, content);
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
