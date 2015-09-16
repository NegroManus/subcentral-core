package de.subcentral.support.predbme;

import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.release.Release;

public class PreDbMePlayground
{

	public static void main(String[] args) throws Exception
	{
		PreDbMeReleaseDb2 lookup = new PreDbMeReleaseDb2();

		URL url = Resources.getResource("de/subcentral/support/predbme/psych.s06e05_p0w4.html");
		Document doc = Jsoup.parse(url.openStream(), "UTF-8", lookup.getHost().toExternalForm());

		Release rls = lookup.parseReleaseRecord(doc);
		System.out.println(rls);
	}
}
