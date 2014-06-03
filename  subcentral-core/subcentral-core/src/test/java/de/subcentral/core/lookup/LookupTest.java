package de.subcentral.core.lookup;

import de.subcentral.core.impl.com.orlydb.OrlyDbLookup;
import de.subcentral.core.impl.com.orlydb.OrlyDbLookupResult;
import de.subcentral.core.impl.com.orlydb.OrlyDbQuery;
import de.subcentral.core.media.Medias;
import de.subcentral.core.release.MediaRelease;

public class LookupTest
{
	public static void main(String[] args) throws LookupException
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		OrlyDbLookup lookup = new OrlyDbLookup();
		OrlyDbQuery query = new OrlyDbQuery();
		query.setQuery(OrlyDbQuery.buildQuery(Medias.newEpisode("Psych", 8, 5)));
		OrlyDbLookupResult result = lookup.lookup("Psych.S08E05");

		for (MediaRelease rls : result.getAllResults())
		{
			System.out.println(rls.getName());
		}
	}
}
