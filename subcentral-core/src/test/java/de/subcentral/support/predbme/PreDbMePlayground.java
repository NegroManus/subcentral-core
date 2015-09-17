package de.subcentral.support.predbme;

import java.util.List;

import de.subcentral.core.metadata.release.Release;

public class PreDbMePlayground
{

	public static void main(String[] args) throws Exception
	{
		PreDbMeReleaseDb2 db = new PreDbMeReleaseDb2();

		// URL url = Resources.getResource("de/subcentral/support/predbme/psych.s06e05_p0w4.html");
		// Document doc = Jsoup.parse(url.openStream(), "UTF-8", db.getHost().toExternalForm());
		//
		// Release rls = db.parseRecord(doc, Release.class);
		// System.out.println(rls);

		List<Release> rlss = db.searchReleasesBySeries("psych");
		rlss.stream().forEach((Release r) -> System.out.println(r));

	}
}
