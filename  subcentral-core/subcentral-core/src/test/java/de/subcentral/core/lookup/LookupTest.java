package de.subcentral.core.lookup;

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
		MediaRelease rls = Releases.newMediaRelease(epi, null, "XviD");

		// OrlyDbLookup lookup = new OrlyDbLookup();
		// lookup.setQueryEntityNamingService(OrlyDb.getOrlyDbQueryNamingService());
		// LookupQuery<MediaRelease> query = lookup.createQueryFromParameters(new OrlyDbLookupParameters("", "Psych S05E06"));
		// for (MediaRelease foundRls : query.getResults())
		// {
		// System.out.println(foundRls);
		// }

		XRelLookup xrelLookup = new XRelLookup();
		LookupQuery<MediaRelease> xrelQuery = xrelLookup.createQuery("Psych");
		for (MediaRelease foundRls : xrelQuery.getResults())
		{
			System.out.println(foundRls);
		}

		// URL resource = Resources.getResource("de/subcentral/core/impl/to/xrel/psych.s08e01.html");
		// Document doc = Jsoup.parse(new File(resource.toURI()), "UTF-8");
		// List<MediaRelease> results = XRelLookupQuery.parseReleases(new URL("http://xrel.to"), doc);
		// for (MediaRelease foundRls : results)
		// {
		// System.out.println(foundRls);
		// }
	}
}
