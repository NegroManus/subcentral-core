package de.subcentral.core.lookup;

import java.net.MalformedURLException;

import de.subcentral.core.impl.com.orlydb.OrlyDb;
import de.subcentral.core.impl.com.orlydb.OrlyDbLookup;
import de.subcentral.core.impl.com.orlydb.OrlyDbLookupResult;
import de.subcentral.core.impl.com.orlydb.OrlyDbQuery;
import de.subcentral.core.media.Episode;
import de.subcentral.core.media.Movie;
import de.subcentral.core.release.MediaRelease;
import de.subcentral.core.release.Releases;

public class LookupTest
{
	public static void main(String[] args) throws LookupException, MalformedURLException
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Episode epi = Episode.newSeasonedEpisode("Psych", 6, 5);
		Movie movie = new Movie("The Lord of the Rings: The Return of the King");
		MediaRelease rls = Releases.newMediaRelease(epi, null);

		OrlyDbLookup lookup = new OrlyDbLookup();
		lookup.setQueryNamingService(OrlyDb.getOrlyDbQueryNamingService());
		OrlyDbQuery query = lookup.createQuery(rls);
		OrlyDbLookupResult result = lookup.lookup(query);

		System.out.println("Results for: " + result.getUrl());
		for (MediaRelease foundRls : result.getAllResults())
		{
			System.out.println(foundRls);
		}
	}
}
