package de.subcentral.support.predbme;

import java.io.File;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

import de.subcentral.core.metadata.release.Release;

public class PreDbMePlayground
{

	public static void main(String[] args) throws Exception
	{
		PreDbMeInfoDb lookup = new PreDbMeInfoDb();

		URL url = Resources.getResource("de/subcentral/support/predbme/icarly.s01e10_details_formatted.html");
		File resource = new File(url.toURI());
		Document doc = Jsoup.parse(resource, "UTF-8", lookup.getHost().toExternalForm());

		Release rls = lookup.parseReleaseDetails(doc, new Release());
		System.out.println(rls);
	}
}
