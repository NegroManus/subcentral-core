package de.subcentral.core.standardizing;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class ClassBasedStandardizingService implements StandardizingService
{
	private final String												domain;
	private final ListMultimap<Class<?>, Standardizer<?>>				standardizers			= LinkedListMultimap.create();
	private final ReentrantReadWriteLock								standardizersRwl		= new ReentrantReadWriteLock();
	private final Map<Class<?>, Function<?, List<? extends Object>>>	nestedBeansRetrievers	= new ConcurrentHashMap<>(8);

	public ClassBasedStandardizingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	/**
	 * 
	 * @return an immutable copy of the current standardizers map (a snapshot)
	 */
	public ImmutableListMultimap<Class<?>, Standardizer<?>> getStandardizers()
	{
		standardizersRwl.readLock().lock();
		try
		{
			return ImmutableListMultimap.copyOf(standardizers);
		}
		finally
		{
			standardizersRwl.readLock().unlock();
		}
	}

	public <T> boolean registerStandardizer(Class<T> entityType, Standardizer<? super T> standardizer)
	{
		standardizersRwl.writeLock().lock();
		try
		{
			return standardizers.put(entityType, standardizer);
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public <T> boolean registerAllStandardizers(Class<T> entityType, Iterable<Standardizer<? super T>> standardizers)
	{
		standardizersRwl.writeLock().lock();
		try
		{
			return this.standardizers.putAll(entityType, standardizers);
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public <T> boolean unregisterStandardizer(Class<T> entityType, Standardizer<? super T> standardizer)
	{
		standardizersRwl.writeLock().lock();
		try
		{
			return standardizers.remove(entityType, standardizer);
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public <T> List<Standardizer<?>> unregisterAllStandardizers(Class<T> entityType)
	{
		standardizersRwl.writeLock().lock();
		try
		{
			return standardizers.removeAll(entityType);
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public int unregisterAllStandardizers()
	{
		standardizersRwl.writeLock().lock();
		try
		{
			int size = standardizers.size();
			standardizers.clear();
			return size;
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public <T> void setAllStandardizers(ListMultimap<Class<?>, Standardizer<?>> parsers)
	{
		standardizersRwl.writeLock().lock();
		try
		{
			this.standardizers.clear();
			this.standardizers.putAll(parsers);
		}
		finally
		{
			standardizersRwl.writeLock().unlock();
		}
	}

	public <T> void registerNestedBeansRetriever(Class<T> type, Function<? super T, List<? extends Object>> retriever)
	{
		nestedBeansRetrievers.put(type, retriever);
	}

	@SuppressWarnings("unchecked")
	private <T> Function<? super T, List<? extends Object>> getNestedBeansRetriever(T bean)
	{
		return (Function<? super T, List<? extends Object>>) nestedBeansRetrievers.get(bean.getClass());
	}

	@Override
	public List<StandardizingChange> standardize(Object bean)
	{
		if (bean == null)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
		// keep track which Objects were already standardized
		// to not end in an infinite loop because two beans had a bidirectional relationship
		IdentityHashMap<Object, Boolean> alreadyStdizedObjs = new IdentityHashMap<>();

		Queue<Object> beansToStandardize = new ArrayDeque<>();
		beansToStandardize.add(bean);
		Object beanToStandardize;
		while ((beanToStandardize = beansToStandardize.poll()) != null)
		{
			if (!alreadyStdizedObjs.containsKey(beanToStandardize))
			{
				changes.addAll(doStandardize(beanToStandardize));
				addNestedBeans(beanToStandardize, beansToStandardize);
				alreadyStdizedObjs.put(beanToStandardize, Boolean.TRUE);
			}
		}
		return changes.build();
	}

	@SuppressWarnings("unchecked")
	private <T> List<StandardizingChange> doStandardize(T bean)
	{
		if (bean == null)
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
		standardizersRwl.readLock().lock();
		try
		{
			for (Standardizer<?> std : standardizers.get(bean.getClass()))
			{
				changes.addAll(((Standardizer<T>) std).standardize(bean));
			}
		}
		finally
		{
			standardizersRwl.readLock().unlock();
		}
		return changes.build();
	}

	private <T> void addNestedBeans(T bean, Queue<Object> queue)
	{
		Function<? super T, List<? extends Object>> nestedBeanRetriever = getNestedBeansRetriever(bean);
		if (nestedBeanRetriever != null)
		{
			for (Object nestedBean : nestedBeanRetriever.apply(bean))
			{
				if (nestedBean != null)
				{
					queue.add(nestedBean);
				}
			}
		}
	}
}
