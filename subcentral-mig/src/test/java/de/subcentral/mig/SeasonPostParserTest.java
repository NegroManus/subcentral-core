package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.subtitle.SubtitleRelease;
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
	public void testParseCsiMiamiS07() throws IOException
	{
		parse("CSI: Miami - Staffel 7 - [DE-Subs: 25 | VO-Subs: 25] - [Komplett]", "post-csimiami_s07.html");
	}

	@Test
	public void testParseTopic() throws IOException
	{
		String s = "'Til Death - Staffel 1-2 - [DE-Subs: 15 | VO-Subs: 15] - [Komplett]";
		SeasonPostContent data = parser.parseTopic(s);
		printSeasonThreadContent(data);
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
		System.out.println("Series: " + content.getSeries());
		System.out.println("Seasons:");
		for (Season season : content.getSeasons())
		{
			System.out.println(season);
			System.out.println("Episodes:");
			for (Episode epi : season.getEpisodes())
			{
				System.out.println(epi);
			}
			System.out.println();
		}

		System.out.println();
		System.out.println("Subs:");
		for (SubtitleRelease subAdj : content.getSubtitleFiles())
		{
			System.out.println(subAdj);
		}
	}

}
