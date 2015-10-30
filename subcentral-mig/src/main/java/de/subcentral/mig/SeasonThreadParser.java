package de.subcentral.mig;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import de.subcentral.core.metadata.media.Season;
import de.subcentral.core.metadata.media.Series;
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

		parsePostTitle(season, postTitle);
		System.out.println(postContent);

		return season;
	}

	/**
	 * <pre>
	 * Numbered:
	 * Mr. Robot - Staffel 1 - [DE-Subs: 10 | VO-Subs: 10] - [Komplett] - [+ Deleted Scenes]
	 * 
	 * Special with title:
	 * Doctor Who - Klassische Folgen - [DE-Subs: 111 | VO-Subs: 160 | Aired: 160] - [+Specials]
	 * Psych - Webisodes - [DE-Subs: 06 | VO-Subs: 06] - [Komplett]
	 * 
	 * Multiple seasons in one thread:
	 * Buffy the Vampire Slayer - Staffel 1 bis Staffel 7 - Komplett
	 * </pre>
	 * 
	 */
	private void parsePostTitle(Season season, String postTitle)
	{
		Matcher numberedMatcher = Pattern.compile("(.*)\\s*-\\s*Staffel\\s+(\\d+)\\s*-\\s*.*").matcher(postTitle);
		if (numberedMatcher.matches())
		{
			Series series = new Series(numberedMatcher.group(1));
			season.setSeries(series);
			Integer number = Integer.valueOf(numberedMatcher.group(2));
			season.setNumber(number);
			return;
		}

		Matcher specialMatcher = Pattern.compile("(.*)\\s*-\\s*([\\w\\s]+)\\s*-\\s*.*").matcher(postTitle);
		if (specialMatcher.matches())
		{
			Series series = new Series(specialMatcher.group(1));
			season.setSeries(series);
			String title = specialMatcher.group(2);
			season.setTitle(title);
			season.setSpecial(true);
			return;
		}

		Matcher multipleMatcher = Pattern.compile("(.*)\\s*-\\s*Staffel\\s+(\\d+)\\s+bis\\s+Staffel\\s+(\\d+)\\s*-\\s*.*").matcher(postTitle);
		if (multipleMatcher.matches())
		{
			Series series = new Series(multipleMatcher.group(1));
			season.setSeries(series);
			String title = specialMatcher.group(2);
			season.setTitle(title);
			season.setSpecial(true);
			return;
		}

	}

	public static void main(String[] args) throws IOException
	{
		SubCentralApi api = new SubCentralHttpApi();
		api.login("NegroManus", "xxx");
		new SeasonThreadParser().getAndParse(43334, api);
	}
}
