package de.subcentral.core.metadata.service;

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

public class MetadataServiceUtil {
    private static final Logger log = LogManager.getLogger(MetadataServiceUtil.class);

    public static <T> ListMultimap<MetadataService, T> searchInAll(List<MetadataService> metadataServices, Object queryObj, Class<T> recordType, ExecutorService executor) throws InterruptedException {
        if (queryObj == null) {
            // if metadataObj is null, don't invoke any threads but return immediately
            return ImmutableListMultimap.of();
        }
        List<Callable<List<T>>> tasks = new ArrayList<>(metadataServices.size());
        for (MetadataService metadataService : metadataServices) {
            tasks.add(() -> metadataService.searchByObject(queryObj, recordType));
        }

        List<Future<List<T>>> futures = executor.invokeAll(tasks);

        ImmutableListMultimap.Builder<MetadataService, T> results = ImmutableListMultimap.builder();
        for (int i = 0; i < metadataServices.size(); i++) {
            try {
                results.putAll(metadataServices.get(i), futures.get(i).get());
            }
            catch (ExecutionException e) {
                log.debug("Exception while searching metadata database " + metadataServices.get(i) + " for records of type " + recordType.getName() + " by " + queryObj
                        + ". Skipping this metadata database.", e);
            }
        }
        return results.build();
    }

    private MetadataServiceUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
