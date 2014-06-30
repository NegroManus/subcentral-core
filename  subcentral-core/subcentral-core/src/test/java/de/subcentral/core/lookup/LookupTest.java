package de.subcentral.core.lookup;

import java.io.File;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.io.Resources;

import de.subcentral.core.impl.to.xrel.XRelLookup;
import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Movie;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Releases;

public class LookupTest
{
	public static void main(String[] args) throws Exception
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Episode epi = Episode.newSeasonedEpisode("Psych", 6, 5);
		Movie movie = new Movie("The Lord of the Rings: The Return of the King");
		MediaRelease rls = Releases.newMediaRelease(epi, null);

		// OrlyDbLookup lookup = new OrlyDbLookup();
		// lookup.setQueryNamingService(OrlyDb.getOrlyDbQueryNamingService());
		// OrlyDbQuery query = lookup.createQuery(rls);
		// OrlyDbLookupResult result = lookup.lookup("Psych S06E05");
		//
		// System.out.println("Results for: " + result.getUrl());
		// for (MediaRelease foundRls : result.getResults())
		// {
		// System.out.println(foundRls);
		// }

		XRelLookup xrelLookup = new XRelLookup();
		URL resource = Resources.getResource("de/subcentral/core/impl/to/xrel/psych.s08e01.html");
		Document doc = Jsoup.parse(new File(resource.toURI()), "UTF-8");
		LookupResult<MediaRelease> xrelResult = xrelLookup.parseDocument(new URL("http://xrel.to"), doc);
		for (MediaRelease foundRls : xrelResult.getResults())
		{
			System.out.println(foundRls);
		}
	}
}
