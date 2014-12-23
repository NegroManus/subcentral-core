package de.subcentral.core.infodb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

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
		List<Callable<List<R>>> tasks = new ArrayList<>(infoDbs.size());
		for (InfoDb<R, ?> infoDb : infoDbs)
		{
			tasks.add(() -> infoDb.queryWithName(queryObj));
		}

		List<Future<List<R>>> futures = executor.invokeAll(tasks);

		ImmutableListMultimap.Builder<InfoDb<R, ?>, R> results = ImmutableListMultimap.builder();
		for (int i = 0; i < infoDbs.size(); i++)
		{
			try
			{
				results.putAll(infoDbs.get(i), futures.get(i).get());
			}
			catch (ExecutionException e)
			{
				log.error("Exception while querying InfoDb " + infoDbs.get(i) + " with query " + queryObj + ". Skipping this InfoDb.", e.getCause());
			}
		}
		return results.build();
	}

	private InfoDbs()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
