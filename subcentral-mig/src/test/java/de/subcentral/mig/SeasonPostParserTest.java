package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleFile;
import de.subcentral.mig.process.SeasonPostParser;
import de.subcentral.mig.process.SeasonPostParser.SeasonPostContent;

public class SeasonPostParserTest
{
	private SeasonPostParser parser = new SeasonPostParser();

	@Test
	public void testParsePsychS05() throws IOException
	{
		parse("Psych - Staffel 5 - [DE-Subs: 16 | VO-Subs: 16] - [Komplett]", "post-psych_s05.html");
	}

	@Test
	public void testParseAmericanHorrorStoryS01() throws IOException
	{
		parse("American Horror Story - Staffel 2: Asylum - [DE-Subs: 13 | VO-Subs: 13] - [Komplett]", "post-psych_s05.html");
	}

	private void parse(String postTitle, String postContentRresourceFilename) throws IOException
	{
		String content = Resources.toString(Resources.getResource(SeasonPostParserTest.class, postContentRresourceFilename), StandardCharsets.UTF_8);
		SeasonPostContent data = parser.parse(postTitle, content);
		printSeasonThreadContent(data);
	}

	private void printSeasonThreadContent(SeasonPostContent content)
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
		for (SubtitleFile subAdj : content.getSubtitleFiles())
		{
			System.out.println(subAdj);
		}
	}

}
