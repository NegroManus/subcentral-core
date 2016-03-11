package de.subcentral.mig.process;

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

import de.subcentral.mig.process.SubberListParser.SubberListData;
import javafx.concurrent.Task;

public class SubberListParser extends Task<SubberListData>
{
	private static final Logger	log	= LogManager.getLogger(SubberListParser.class);
	private static final String	URL	= "http://subcentral.de/index.php?page=WbbThread&postID=33900#post33900";

	@Override
	protected SubberListData call() throws IOException
	{
		Pattern userIdPattern = Pattern.compile("page=User&userID=(\\d+)");
		Document doc = Jsoup.parse(new URL(URL), 5000);
		SortedSet<ScContributor> subberList = new TreeSet<>();

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
			ScContributor subber = new ScContributor(ScContributor.Type.SUBBER);
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

		return new SubberListData(subberList);
	}

	public static class SubberListData
	{
		private final ImmutableList<ScContributor> subbers;

		public SubberListData(Iterable<ScContributor> subbers)
		{
			this.subbers = ImmutableList.copyOf(subbers);
		}

		public ImmutableList<ScContributor> getSubbers()
		{
			return subbers;
		}
	}

	public static void main(String[] args) throws Exception
	{
		SubberListParser task = new SubberListParser();
		SubberListData content = task.call();
		for (ScContributor subber : content.getSubbers())
		{
			System.out.printf("<contributorPattern pattern=\"(?&lt;!\\w)%s(?!\\w)\" patternMode=\"REGEX\" type=\"SUBBER\" scUserId=\"%s\" />%n", Pattern.quote(subber.getName()), subber.getId());
		}
	}
}
