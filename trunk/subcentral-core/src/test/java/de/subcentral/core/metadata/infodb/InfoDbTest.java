package de.subcentral.core.metadata.infodb;

import java.util.List;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.media.RegularAvMedia;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.support.predbme.PreDbMeInfoDb;

public class InfoDbTest
{
	public static void main(String[] args) throws Exception
	{
		System.getProperties().put("http.proxyHost", "10.206.247.65");
		System.getProperties().put("http.proxyPort", "8080");

		Episode epi = Episode.createSeasonedEpisode("Psych", 6, 5);
		RegularAvMedia movie = new RegularAvMedia("The Lord of the Rings");
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

		PreDbMeInfoDb preDbLookup = new PreDbMeInfoDb();
		List<Release> results = preDbLookup.query("^pacific rim 720p");
		results.forEach(r -> System.out.println(r));

		// File resource = new File(Resources.getResource("de/subcentral/core/impl/predb/psych.s06e05.html").toURI());
		// List<Release> results = preDbLookup.getResults(resource);
		// for (Release foundRls : results)
		// {
		// System.out.println(foundRls);
		// }

		// File detailsRes = new File(Resources.getResource("de/subcentral/core/impl/predb/psych.s06e05_p0w4.html").toURI());
		// Release detailsRls = preDbLookup.parseReleaseDetails(detailsRes);
		// System.out.println(detailsRls);
	}
}