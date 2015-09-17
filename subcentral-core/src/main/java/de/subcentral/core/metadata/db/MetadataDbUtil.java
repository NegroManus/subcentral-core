package de.subcentral.core.metadata.db;

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

public class MetadataDbUtil
{
	private static final Logger log = LogManager.getLogger(MetadataDbUtil.class);

	public static <T> ListMultimap<MetadataDb, T> searchInAll(List<MetadataDb> metadataDbs, Object queryObj, Class<T> recordType, ExecutorService executor) throws InterruptedException
	{
		if (queryObj == null)
		{
			// if metadataObj is null, don't invoke any threads but return immediately
			return ImmutableListMultimap.of();
		}
		List<Callable<List<T>>> tasks = new ArrayList<>(metadataDbs.size());
		for (MetadataDb metadataDb : metadataDbs)
		{
			tasks.add(() -> metadataDb.searchByObject(queryObj, recordType));
		}

		List<Future<List<T>>> futures = executor.invokeAll(tasks);

		ImmutableListMultimap.Builder<MetadataDb, T> results = ImmutableListMultimap.builder();
		for (int i = 0; i < metadataDbs.size(); i++)
		{
			try
			{
				results.putAll(metadataDbs.get(i), futures.get(i).get());
			}
			catch (ExecutionException e)
			{
				log.debug(
						"Exception while searching metadata database " + metadataDbs.get(i) + " for records of type " + recordType.getName() + " by " + queryObj + ". Skipping this metadata database.",
						e);
			}
		}
		return results.build();
	}

	private MetadataDbUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
