package de.subcentral.core.standardizing;

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class ClassBasedStandardizingService implements StandardizingService
{
	private final String												domain;
	private final List<StandardizerEntry<?>>							standardizerEntries		= new CopyOnWriteArrayList<>();
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

	public List<StandardizerEntry<?>> getStandardizerEntries()
	{
		return standardizerEntries;
	}

	public <T> void registerStandardizer(Class<T> beanType, Standardizer<? super T> standardizer)
	{
		standardizerEntries.add(new StandardizerEntry<T>(standardizer, beanType));
	}

	public boolean unregisterStandardizer(Standardizer<?> standardizer)
	{
		for (StandardizerEntry<?> entry : standardizerEntries)
		{
			if (entry.standardizer.equals(standardizer))
			{
				standardizerEntries.remove(entry);
				return true;
			}
		}
		return false;
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
			changes.addAll(doStandardize(beanToStandardize));
			alreadyStdizedObjs.put(beanToStandardize, Boolean.TRUE);
			addNestedBeans(beanToStandardize, beansToStandardize, alreadyStdizedObjs);
		}
		return changes.build();
	}

	@SuppressWarnings("unchecked")
	private <T> List<StandardizingChange> doStandardize(T bean)
	{
		ImmutableList.Builder<StandardizingChange> changes = ImmutableList.builder();
		for (StandardizerEntry<?> entry : standardizerEntries)
		{
			if (entry.beanType.isAssignableFrom(bean.getClass()))
			{
				Standardizer<? super T> standardizer = (Standardizer<? super T>) entry.standardizer;
				changes.addAll(standardizer.standardize(bean));
			}
		}
		return changes.build();
	}

	private <T> void addNestedBeans(T bean, Queue<Object> queue, IdentityHashMap<Object, Boolean> alreadyStdizedObjs)
	{
		Function<? super T, List<? extends Object>> nestedBeanRetriever = getNestedBeansRetriever(bean);
		if (nestedBeanRetriever != null)
		{
			for (Object nestedBean : nestedBeanRetriever.apply(bean))
			{
				if (nestedBean != null && !alreadyStdizedObjs.containsKey(nestedBean))
				{
					queue.add(nestedBean);
				}
			}
		}
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(ClassBasedStandardizingService.class).add("domain", domain).toString();
	}

	public static final class StandardizerEntry<T>
	{
		private final Standardizer<? super T>	standardizer;
		private final Class<T>					beanType;

		private StandardizerEntry(Standardizer<? super T> standardizer, Class<T> beanType)
		{
			this.standardizer = Objects.requireNonNull(standardizer, "standardizer");
			this.beanType = Objects.requireNonNull(beanType, "beanType");
		}

		public Standardizer<? super T> getStandardizer()
		{
			return standardizer;
		}

		public Class<T> getBeanType()
		{
			return beanType;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(StandardizerEntry.class).add("beanType", beanType).add("standardizer", standardizer).toString();
		}
	}
}
