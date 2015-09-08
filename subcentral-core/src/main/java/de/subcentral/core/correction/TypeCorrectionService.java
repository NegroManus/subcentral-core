package de.subcentral.core.correction;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
public class TypeCorrectionService implements CorrectionService
{
	private final String												domain;
	private final List<StandardizerEntry<?>>							standardizerEntries		= new CopyOnWriteArrayList<>();
	private final Map<Class<?>, Function<?, List<? extends Object>>>	nestedBeansRetrievers	= new ConcurrentHashMap<>(8);

	public TypeCorrectionService(String domain)
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

	public <T> void registerStandardizer(Class<T> beanType, Corrector<? super T> standardizer)
	{
		standardizerEntries.add(new StandardizerEntry<T>(standardizer, beanType));
	}

	public boolean unregisterStandardizer(Corrector<?> standardizer)
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

	public <T> void registerNestedBeansRetriever(Class<T> beanType, Function<? super T, List<?>> retriever)
	{
		nestedBeansRetrievers.put(beanType, retriever);
	}

	@Override
	public List<Correction> correct(Object bean)
	{
		if (bean == null)
		{
			return ImmutableList.of();
		}
		List<Correction> changes = new ArrayList<>();
		// keep track which beans were already standardized
		// to not end in an infinite loop because two beans had a bidirectional relationship
		IdentityHashMap<Object, Boolean> alreadyStdizedBeans = new IdentityHashMap<>();

		Queue<Object> beansToStandardize = new ArrayDeque<>();
		beansToStandardize.add(bean);
		Object beanToStandardize;
		while ((beanToStandardize = beansToStandardize.poll()) != null)
		{
			doStandardize(beanToStandardize, changes);
			alreadyStdizedBeans.put(beanToStandardize, Boolean.TRUE);
			addNestedBeans(beanToStandardize, beansToStandardize, alreadyStdizedBeans);
		}
		return changes;
	}

	@SuppressWarnings("unchecked")
	private <T> void doStandardize(T bean, List<Correction> changes)
	{
		for (StandardizerEntry<?> entry : standardizerEntries)
		{
			if (entry.beanType.isAssignableFrom(bean.getClass()))
			{
				Corrector<? super T> standardizer = (Corrector<? super T>) entry.standardizer;
				standardizer.correct(bean, changes);
			}
		}
	}

	private <T> void addNestedBeans(T bean, Queue<Object> queue, IdentityHashMap<Object, Boolean> alreadyStdizedBeans)
	{
		Function<? super T, List<? extends Object>> nestedBeanRetriever = getNestedBeansRetriever(bean);
		if (nestedBeanRetriever != null)
		{
			for (Object nestedBean : nestedBeanRetriever.apply(bean))
			{
				if (nestedBean != null && !alreadyStdizedBeans.containsKey(nestedBean))
				{
					queue.add(nestedBean);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> Function<? super T, List<? extends Object>> getNestedBeansRetriever(T bean)
	{
		return (Function<? super T, List<? extends Object>>) nestedBeansRetrievers.get(bean.getClass());
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(TypeCorrectionService.class).add("domain", domain).toString();
	}

	public static final class StandardizerEntry<T>
	{
		private final Corrector<? super T>	standardizer;
		private final Class<T>					beanType;

		private StandardizerEntry(Corrector<? super T> standardizer, Class<T> beanType)
		{
			this.standardizer = Objects.requireNonNull(standardizer, "standardizer");
			this.beanType = Objects.requireNonNull(beanType, "beanType");
		}

		public Corrector<? super T> getStandardizer()
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
