package de.subcentral.mig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.google.common.io.Resources;

import de.subcentral.mig.SeasonThreadParser.SeasonThreadData;

public class SeasonThreadParserTest
{
	private SeasonThreadParser parser = new SeasonThreadParser();

	@Test
	public void testParseMrRobotS01() throws IOException
	{
		Document doc = parseDoc("thread-mrrobot_s01.html");
		SeasonThreadData data = parser.parse(doc);
		System.out.println(data);
	}

	private Document parseDoc(String filename) throws IOException
	{
		return Jsoup.parse(Resources.getResource(SeasonThreadParserTest.class, filename).openStream(), StandardCharsets.UTF_8.name(), "http://subcentral.de");
	}
}
