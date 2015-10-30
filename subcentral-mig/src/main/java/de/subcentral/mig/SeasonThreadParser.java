package de.subcentral.mig;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.support.subcentralde.SubCentralApi;
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class SeasonThreadParser
{
	public Season parse(SubCentralApi api, Season season) throws IOException
	{
		Integer threadId = season.getAttributeValue(SeriesListParser.ATTRIBUTE_THREAD_ID);
		if (threadId == null)
		{
			throw new IllegalArgumentException("No threadID set for this season");
		}

		Document doc = api.getContent("index.php?page=Thread&threadID=" + threadId);

		Element postContentElem = doc.getElementsByClass("messageBody").first();
		System.out.println(postContentElem);

		return season;
	}

	public static void main(String[] args) throws IOException
	{
		SubCentralApi api = new SubCentralHttpApi();
		api.login("NegroManus", "xxx");

		Path attachment = api.downloadAttachment(30462, Paths.get(SystemUtils.USER_HOME));
		System.out.println("Downloaded attachment to " + attachment);

		// Season season = new Season(new Series("Mr. Robot"), 1);
		// season.getAttributes().put(SeriesListParser.ATTRIBUTE_THREAD_ID, 42616);
		//
		// SeasonThreadParser parser = new SeasonThreadParser();
		// parser.parse(api, season);
	}
}
