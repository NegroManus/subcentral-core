package de.subcentral.mig;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.support.subcentralde.SubCentralApi;
import de.subcentral.support.subcentralde.SubCentralHttpApi;

public class SeasonThreadParser
{
	public Season getAndParse(int threadId, SubCentralApi api) throws IOException
	{
		Document doc = api.getContent("index.php?page=Thread&threadID=" + threadId);
		return parse(doc);
	}

	public Season parse(Document threadHtml)
	{
		// Get title and content of first post
		Element postTitleDiv = threadHtml.getElementsByClass("messageTitle").first();
		Element postContentDiv = threadHtml.getElementsByClass("messageBody").first();
		if (postTitleDiv == null || postContentDiv == null)
		{
			throw new IllegalArgumentException("Invalid thread html: No post found");
		}
		Element postTextDiv = postContentDiv.child(0);

		return parse(postTitleDiv.text(), postTextDiv.html());
	}

	public Season parse(String postTitle, String postContent)
	{
		Season season = new Season();

		System.out.println(postTitle);
		System.out.println(postContent);

		return season;
	}

	/**
	 * <pre>
	 * Mr. Robot - Staffel 1 - [DE-Subs: 10 | VO-Subs: 10] - [Komplett] - [+ Deleted Scenes]
	 * </pre>
	 * 
	 */
	private void parsePostTitle(Season season, String postTitle)
	{

	}

	public static void main(String[] args) throws IOException
	{
		SubCentralApi api = new SubCentralHttpApi();
		api.login("NegroManus", "xxx");
		new SeasonThreadParser().getAndParse(43334, api);
	}
}
