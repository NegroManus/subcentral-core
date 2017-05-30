package de.subcentral.core.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;

public class ConcurrentUtil {
    private static final Logger log = LogManager.getLogger(ConcurrentUtil.class);

    public static <T> List<T> executeAll(List<Callable<T>> tasks, ExecutorService executor) throws InterruptedException {
        List<Future<T>> futures = executor.invokeAll(tasks);

        ImmutableList.Builder<T> results = ImmutableList.builder();
        for (int i = 0; i < futures.size(); i++) {
            try {
                results.add(futures.get(i).get());
            }
            catch (ExecutionException e) {
                log.debug("Exception while executing task " + i, e);
            }
        }
        return results.build();
    }

    private ConcurrentUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
