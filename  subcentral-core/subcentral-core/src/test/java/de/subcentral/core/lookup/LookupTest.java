package de.subcentral.core.lookup;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.release.MediaRelease;
import de.subcentral.core.model.release.Releases;
import de.subcentral.thirdparty.com.orlydb.OrlyDbLookup;

public class LookupTest
{
	public static void main(String[] args) throws Exception
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Episode epi = Episode.newSeasonedEpisode("Psych", 6, 5);
		Movie movie = new Movie("The Lord of the Rings");
		MediaRelease rls = Releases.newMediaRelease(movie, null, "720p");

		OrlyDbLookup lookup = new OrlyDbLookup();
		LookupQuery<MediaRelease> query = lookup.createQueryFromEntity(epi);
		for (MediaRelease foundRls : query.getResults())
		{
			System.out.println(foundRls);
		}

		// XRelLookup xrelLookup = new XRelLookup();
		// LookupQuery<MediaRelease> xrelQuery = xrelLookup.createQuery("Psych");
		// for (MediaRelease foundRls : xrelQuery.getResults())
		// {
		// System.out.println(foundRls);
		// }

		// File resource = new File(Resources.getResource("de/subcentral/core/impl/to/xrel/psych.s05e06.html").toURI());
		// List<MediaRelease> results = new XRelLookup().getResults(resource);
		// for (MediaRelease foundRls : results)
		// {
		// System.out.println(foundRls);
		// }
	}
}
