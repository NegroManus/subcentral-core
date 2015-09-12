package de.subcentral.core.metadata.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.metadata.media.Episode;
import de.subcentral.core.metadata.release.Release;
import de.subcentral.core.metadata.release.ReleaseUtil;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.orlydbcom.OrlyDbComReleaseDb;
import de.subcentral.support.predbme.PreDbMeReleaseDb;
import de.subcentral.support.xrelto.XRelToReleaseDb;

public class MultiInfoDbPlayground
{

	/**
	 * <pre>
	 * -Dhttp.proxyHost=10.151.249.76 -Dhttp.proxyPort=8080
	 * </pre>
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException
	{
		PreDbMeReleaseDb preDbMe = new PreDbMeReleaseDb();
		XRelToReleaseDb xrelTo = new XRelToReleaseDb();
		OrlyDbComReleaseDb orlyDb = new OrlyDbComReleaseDb();
		List<MetadataDb<Release>> infoDbs = new ArrayList<>(3);
		// infoDbs.add(preDbMe);
		infoDbs.add(xrelTo);
		// infoDbs.add(orlyDb);

		Episode epi1 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 23);
		Episode epi2 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 24);
		List<Episode> query = ImmutableList.of(epi1, epi2);
		// RegularMedia query = new RegularMedia("Halo.Nightfall");

		ExecutorService executor = Executors.newFixedThreadPool(3);

		System.out.println("Querying");
		long start = System.nanoTime();
		ListMultimap<MetadataDb<Release>, Release> results = MetadataDbUtil.queryAll(infoDbs, query, executor);
		TimeUtil.printDurationMillis("queryAll", start);
		for (Map.Entry<MetadataDb<Release>, Collection<Release>> entry : results.asMap().entrySet())
		{
			System.out.println("Results of " + entry.getKey().getDisplayName() + " " + entry.getKey().getDomain());
			entry.getValue().stream().forEach((r) -> System.out.println(r));
			System.out.println();
		}
		executor.shutdown();

		List<Release> reducedRlss = ReleaseUtil.distinctByName(results.values());
		reducedRlss.stream().forEach(e -> System.out.println(e));
	}
}
