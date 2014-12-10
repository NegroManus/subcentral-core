package de.subcentral.core.infodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.RegularAvMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.core.model.release.Releases;
import de.subcentral.core.util.TimeUtil;
import de.subcentral.support.orlydbcom.OrlyDbComInfoDb;
import de.subcentral.support.predbme.PreDbMeInfoDb;
import de.subcentral.support.xrelto.XRelToInfoDb;

public class MultiInfoDbPlayground
{

	public static void main(String[] args) throws InterruptedException
	{
		PreDbMeInfoDb preDbMe = new PreDbMeInfoDb();
		XRelToInfoDb xrelTo = new XRelToInfoDb();
		OrlyDbComInfoDb orlyDb = new OrlyDbComInfoDb();
		List<InfoDb<Release, ?>> infoDbs = new ArrayList<>(3);
		infoDbs.add(preDbMe);
		infoDbs.add(xrelTo);
		infoDbs.add(orlyDb);

		// Episode query = Episode.createSeasonedEpisode("Big Bang Theory", 8, 10);
		RegularAvMedia query = new RegularAvMedia("Halo.Nightfall");

		ExecutorService executor = Executors.newFixedThreadPool(3);

		System.out.println("Querying");
		long start = System.nanoTime();
		ListMultimap<InfoDb<Release, ?>, Release> results = InfoDbs.queryAll(infoDbs, query, executor);
		TimeUtil.printDurationMillis("queryAll", start);
		for (Map.Entry<InfoDb<Release, ?>, Collection<Release>> entry : results.asMap().entrySet())
		{
			System.out.println("Results of " + entry.getKey().getName() + " " + entry.getKey().getDomain());
			entry.getValue().stream().forEach((r) -> System.out.println(r));
			System.out.println();
		}
		executor.shutdown();

		List<Release> reducedRlss = Releases.distinctReleasesByName(results);
		reducedRlss.stream().forEach(e -> System.out.println(e));
	}
}
