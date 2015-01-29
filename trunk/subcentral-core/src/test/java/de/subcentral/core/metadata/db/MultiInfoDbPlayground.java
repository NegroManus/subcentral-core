package de.subcentral.core.metadata.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.db.MetadataDb;
import de.subcentral.core.metadata.db.MetadataDbUtils;
import de.subcentral.core.metadata.media.RegularAvMedia;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtils;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.orlydbcom.OrlyDbComReleaseDb;
import de.subcentral.support.predbme.PreDbMeReleaseDb;
import de.subcentral.support.xrelto.XRelToReleaseDb;

public class MultiInfoDbPlayground
{

	public static void main(String[] args) throws InterruptedException
	{
		PreDbMeReleaseDb preDbMe = new PreDbMeReleaseDb();
		XRelToReleaseDb xrelTo = new XRelToReleaseDb();
		OrlyDbComReleaseDb orlyDb = new OrlyDbComReleaseDb();
		List<MetadataDb<Release>> infoDbs = new ArrayList<>(3);
		infoDbs.add(preDbMe);
		infoDbs.add(xrelTo);
		infoDbs.add(orlyDb);

		// Episode query = Episode.createSeasonedEpisode("Big Bang Theory", 8, 10);
		RegularAvMedia query = new RegularAvMedia("Halo.Nightfall");

		ExecutorService executor = Executors.newFixedThreadPool(3);

		System.out.println("Querying");
		long start = System.nanoTime();
		ListMultimap<MetadataDb<Release>, Release> results = MetadataDbUtils.queryAll(infoDbs, query, executor);
		TimeUtil.printDurationMillis("queryAll", start);
		for (Map.Entry<MetadataDb<Release>, Collection<Release>> entry : results.asMap().entrySet())
		{
			System.out.println("Results of " + entry.getKey().getName() + " " + entry.getKey().getDomain());
			entry.getValue().stream().forEach((r) -> System.out.println(r));
			System.out.println();
		}
		executor.shutdown();

		List<Release> reducedRlss = ReleaseUtils.distinctByName(results.values());
		reducedRlss.stream().forEach(e -> System.out.println(e));
	}
}
