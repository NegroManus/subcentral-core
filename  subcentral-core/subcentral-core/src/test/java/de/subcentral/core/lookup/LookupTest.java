package de.subcentral.core.lookup;

import java.io.File;
import java.util.List;

import com.google.common.io.Resources;

import de.subcentral.core.model.media.Episode;
import de.subcentral.core.model.media.Movie;
import de.subcentral.core.model.release.Release;
import de.subcentral.impl.predb.PreDbLookup;

public class LookupTest
{
	public static void main(String[] args) throws Exception
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Episode epi = Episode.createSeasonedEpisode("Psych", 6, 5);
		Movie movie = new Movie("The Lord of the Rings");
		Release rls = Release.create(movie, null, "720p");

		// OrlyDbLookup lookup = new OrlyDbLookup();
		// LookupQuery<Release> query = lookup.createQueryFromEntity(epi);
		// for (Release foundRls : query.getResults())
		// {
		// System.out.println(foundRls);
		// }

		// XRelLookup xrelLookup = new XRelLookup();
		// LookupQuery<MediaRelease> xrelQuery = xrelLookup.createQuery("Psych");
		// for (MediaRelease foundRls : xrelQuery.getResults())
		// {
		// System.out.println(foundRls);
		// }

		PreDbLookup preDbLookup = new PreDbLookup();

		File resource = new File(Resources.getResource("de/subcentral/core/impl/predb/psych.s06e05.html").toURI());
		List<Release> results = preDbLookup.getResults(resource);
		for (Release foundRls : results)
		{
			System.out.println(foundRls);
		}
	}
}
