package de.subcentral.core.standardizing;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;

public class ClassBasedStandardizingService implements StandardizingService
{
	private final String												domain;
	private final ListMultimap<Class<?>, Standardizer<?>>				standardizers			= LinkedListMultimap.create();
	private final Map<Class<?>, Function<?, List<? extends Object>>>	nestedBeansRetrievers	= new HashMap<>(8);

	public ClassBasedStandardizingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public <T> boolean registerStandardizer(Class<T> entityType, Standardizer<T> standardizer)
	{
		return standardizers.put(entityType, standardizer);
	}

	public <T> boolean registerAllStandardizers(Class<T> entityType, Iterable<Standardizer<T>> standardizers)
	{
		return this.standardizers.putAll(entityType, standardizers);
	}

	public <T> boolean unregisterStandardizer(Class<T> entityType, Standardizer<T> standardizer)
	{
		return standardizers.remove(entityType, standardizer);
	}

	public <T> List<Standardizer<?>> unregisterAllStandardizers(Class<T> entityType)
	{
		return standardizers.removeAll(entityType);
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
		IdentityHashMap<Object, Boolean> standardizedObjects = new IdentityHashMap<>();

		Queue<Object> beansToStandardize = new ArrayDeque<>();
		beansToStandardize.add(bean);
		Object beanToStandardize;
		while ((beanToStandardize = beansToStandardize.poll()) != null)
		{
			if (!standardizedObjects.containsKey(beanToStandardize))
			{
				changes.addAll(doStandardize(beanToStandardize));
				addNestedBeans(beanToStandardize, beansToStandardize);
				standardizedObjects.put(beanToStandardize, Boolean.TRUE);
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
		for (Standardizer<?> std : standardizers.get(bean.getClass()))
		{
			changes.addAll(((Standardizer<T>) std).standardize(bean));
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
