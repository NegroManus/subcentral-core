package de.subcentral.core.infodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.model.media.RegularAvMedia;
import de.subcentral.core.model.release.Release;
import de.subcentral.support.orlydbcom.OrlyDbComInfoDb;
import de.subcentral.support.predbme.PreDbMeInfoDb;
import de.subcentral.support.xrelto.XRelToInfoDb;

public class InfoDbs
{
	private static final Logger	log	= LogManager.getLogger(InfoDbs.class);

	public static <R> ListMultimap<InfoDb<R, ?>, R> queryAll(List<InfoDb<R, ?>> infoDbs, Object queryObj, ExecutorService executor)
			throws InterruptedException
	{
		if (infoDbs.isEmpty() || queryObj == null)
		{
			return ImmutableListMultimap.of();
		}
		ImmutableList.Builder<Callable<List<R>>> tasks = ImmutableList.builder();
		for (InfoDb<R, ?> infoDb : infoDbs)
		{
			tasks.add(() -> infoDb.queryWithName(queryObj));
		}

		List<Future<List<R>>> futures = executor.invokeAll(tasks.build());

		ImmutableListMultimap.Builder<InfoDb<R, ?>, R> results = ImmutableListMultimap.builder();
		for (int i = 0; i < infoDbs.size(); i++)
		{
			try
			{
				results.putAll(infoDbs.get(i), futures.get(i).get());
			}
			catch (ExecutionException e)
			{
				log.error("Exception while querying InfoDb " + infoDbs.get(i) + " with query " + queryObj + ". Skipping this InfoDb.", e);
			}
		}
		return results.build();
	}

	private InfoDbs()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated");
	}

	public static void main(String[] args) throws InterruptedException
	{
		OrlyDbComInfoDb orlyDb = new OrlyDbComInfoDb();
		XRelToInfoDb xrelTo = new XRelToInfoDb();
		PreDbMeInfoDb preDbMe = new PreDbMeInfoDb();
		List<InfoDb<Release, ?>> infoDbs = new ArrayList<>(3);
		infoDbs.add(orlyDb);
		infoDbs.add(xrelTo);
		infoDbs.add(preDbMe);

		// Episode query = Episode.createSeasonedEpisode("Big Bang Theory", 8, 10);
		RegularAvMedia query = new RegularAvMedia("The.Gift.2007");

		ExecutorService executor = Executors.newFixedThreadPool(3);

		System.out.println("Querying");
		ListMultimap<InfoDb<Release, ?>, Release> results = queryAll(infoDbs, query, executor);
		for (Map.Entry<InfoDb<Release, ?>, Collection<Release>> entry : results.asMap().entrySet())
		{
			System.out.println("Results of " + entry.getKey().getName());
			entry.getValue().stream().forEach((r) -> System.out.println(r));
			System.out.println();
		}

		executor.shutdown();
	}
}
