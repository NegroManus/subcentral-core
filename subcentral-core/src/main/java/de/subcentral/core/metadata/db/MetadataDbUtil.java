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

    public static <R> ListMultimap<MetadataDb<R>, R> queryAll(List<MetadataDb<R>> metadataDbs, Object metadataObj, ExecutorService executor) throws InterruptedException
    {
	if (metadataObj == null)
	{
	    // if metadataObj is null, don't invoke any threads but return immediately
	    return ImmutableListMultimap.of();
	}
	List<Callable<List<R>>> tasks = new ArrayList<>(metadataDbs.size());
	for (MetadataDb<R> metadataDb : metadataDbs)
	{
	    tasks.add(() -> metadataDb.queryName(metadataObj));
	}

	List<Future<List<R>>> futures = executor.invokeAll(tasks);

	ImmutableListMultimap.Builder<MetadataDb<R>, R> results = ImmutableListMultimap.builder();
	for (int i = 0; i < metadataDbs.size(); i++)
	{
	    try
	    {
		results.putAll(metadataDbs.get(i), futures.get(i).get());
	    }
	    catch (ExecutionException e)
	    {
		log.debug("Exception while querying metadata database " + metadataDbs.get(i) + " with query " + metadataObj + ". Skipping this metadata database.", e);
	    }
	}
	return results.build();
    }

    private MetadataDbUtil()
    {
	throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
