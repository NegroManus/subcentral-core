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
import de.subcentral.support.orlydbcom.OrlyDbComMetadataDb;
import de.subcentral.support.predbme.PreDbMeMetadataDb;
import de.subcentral.support.xrelto.XRelToMetadataDb;

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
		PreDbMeMetadataDb preDbMe = new PreDbMeMetadataDb();
		XRelToMetadataDb xrelTo = new XRelToMetadataDb();
		OrlyDbComMetadataDb orlyDb = new OrlyDbComMetadataDb();
		List<MetadataDb> dbs = new ArrayList<>(3);
		dbs.add(preDbMe);
		dbs.add(xrelTo);
		dbs.add(orlyDb);

		Episode epi1 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 23);
		Episode epi2 = Episode.createSeasonedEpisode("How I Met Your Mother", 9, 24);
		List<Episode> query = ImmutableList.of(epi1, epi2);
		// AbstractSingleMedia query = new AbstractSingleMedia("Halo.Nightfall");

		ExecutorService executor = Executors.newFixedThreadPool(3);

		System.out.println("Querying");
		long start = System.nanoTime();
		ListMultimap<MetadataDb, Release> results = MetadataDbUtil.searchInAll(dbs, query, Release.class, executor);
		TimeUtil.logDurationMillisDouble("queryAll", start);
		for (Map.Entry<MetadataDb, Collection<Release>> entry : results.asMap().entrySet())
		{
			System.out.println("Results of " + entry.getKey().getSite());
			entry.getValue().stream().forEach((r) -> System.out.println(r));
			System.out.println();
		}
		executor.shutdown();

		System.out.println("All results combined");
		List<Release> reducedRlss = ReleaseUtil.distinctByName(results.values());
		reducedRlss.stream().forEach(e -> System.out.println(e));
	}
}
