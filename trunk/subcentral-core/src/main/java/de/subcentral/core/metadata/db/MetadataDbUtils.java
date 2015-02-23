package de.subcentral.core.metadata.db;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import de.subcentral.core.naming.NamingService;

public class MetadataDbUtils
{
	private static final Logger	log	= LogManager.getLogger(MetadataDbUtils.class);

	public static <R> ListMultimap<MetadataDb<R>, R> queryAll(List<MetadataDb<R>> metadataDbs, Object metadataObj, ExecutorService executor)
			throws InterruptedException
	{
		return queryAll(metadataDbs, metadataObj, ImmutableList.of(MetadataDbDefaults.getDefaultMetadataDbNamingService()), executor);
	}

	public static <R> ListMultimap<MetadataDb<R>, R> queryAll(List<MetadataDb<R>> metadataDbs, Object metadataObj, NamingService namingService,
			ExecutorService executor) throws InterruptedException
	{
		return queryAll(metadataDbs, metadataObj, ImmutableList.of(namingService), executor);
	}

	public static <R> ListMultimap<MetadataDb<R>, R> queryAll(List<MetadataDb<R>> metadataDbs, Object metadataObj,
			List<NamingService> namingServices, ExecutorService executor) throws InterruptedException
	{
		if (metadataDbs.isEmpty() || metadataObj == null)
		{
			return ImmutableListMultimap.of();
		}
		if (namingServices.isEmpty())
		{
			throw new IllegalArgumentException("namingServices is empty. At least one NamingService must be specified");
		}
		List<Callable<List<R>>> tasks = new ArrayList<>(metadataDbs.size());
		for (MetadataDb<R> infoDb : metadataDbs)
		{
			for (NamingService ns : namingServices)
			{
				tasks.add(() -> infoDb.queryWithName(metadataObj, ns));
			}
		}

		List<Future<List<R>>> futures = executor.invokeAll(tasks);

		ImmutableListMultimap.Builder<MetadataDb<R>, R> results = ImmutableListMultimap.builder();
		for (int i = 0; i < metadataDbs.size() * namingServices.size(); i++)
		{
			int indexOfMetaDb = i / namingServices.size();
			try
			{
				results.putAll(metadataDbs.get(indexOfMetaDb), futures.get(i).get());
			}
			catch (ExecutionException e)
			{
				log.error("Exception while querying MetadataDb " + metadataDbs.get(indexOfMetaDb) + " with query " + metadataObj
						+ ". Skipping this MetadataDb.", e);
			}
		}
		return results.build();
	}

	private MetadataDbUtils()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
