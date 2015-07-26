package de.subcentral.mig;

import java.io.IOException;
import java.net.URL;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.ImmutableList;

import de.subcentral.mig.SubberListParser.SubberListContent;
import javafx.concurrent.Task;

public class SubberListParser extends Task<SubberListContent>
{
	private static final Logger	log	= LogManager.getLogger(SubberListParser.class);
	private static final String	URL	= "http://subcentral.de/index.php?page=Thread&postID=33900#post33900";

	@Override
	protected SubberListContent call() throws IOException
	{
		Pattern userIdPattern = Pattern.compile("page=User&userID=(\\d+)");
		Document doc = Jsoup.parse(new URL(URL), 5000);
		SortedSet<Subber> subberList = new TreeSet<>();

		/**
		 * <td><a href="http://www.subcentral.de/index.php?page=User&userID=21359" title= "Benutzerprofil von &raquo; **butterfly**&laquo; aufrufen" >**butterfly**</a></td>
		 */
		Elements userAnchors = doc.select("a[href*=page=User&userID=]");
		for (Element a : userAnchors)
		{
			String name = a.text().replace(" (PROBIE)", "");
			if (name.isEmpty())
			{
				log.warn("Empty user name: {}", a);
				continue;
			}
			Subber subber = new Subber();
			subber.setName(name);
			int id = 0;
			Matcher userIdMatcher = userIdPattern.matcher(a.attr("href"));
			if (userIdMatcher.find())
			{
				id = Integer.parseInt(userIdMatcher.group(1));
			}
			subber.setId(id);
			subberList.add(subber);
		}

		return new SubberListContent(subberList);
	}

	public static class SubberListContent
	{
		private final ImmutableList<Subber> subbers;

		public SubberListContent(Iterable<Subber> subbers)
		{
			this.subbers = ImmutableList.copyOf(subbers);
		}

		public ImmutableList<Subber> getSubbers()
		{
			return subbers;
		}
	}

	public static void main(String[] args) throws Exception
	{
		SubberListParser task = new SubberListParser();
		SubberListContent content = task.call();
		for (Subber subber : content.getSubbers())
		{
			System.out.printf("<contributorPattern pattern=\"(?&lt;!\\w)%s(?!\\w)\" patternMode=\"REGEX\" type=\"SUBBER\" scUserId=\"%s\" />%n", Pattern.quote(subber.getName()), subber.getId());
		}
	}
}
