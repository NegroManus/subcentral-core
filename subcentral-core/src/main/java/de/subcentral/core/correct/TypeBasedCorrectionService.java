package de.subcentral.core.correct;

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
public class TypeBasedCorrectionService implements CorrectionService
{
	private final String												domain;
	private final List<CorrectorEntry<?>>								correctorEntries		= new CopyOnWriteArrayList<>();
	private final Map<Class<?>, Function<?, List<? extends Object>>>	nestedBeansRetrievers	= new ConcurrentHashMap<>(8);

	public TypeBasedCorrectionService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public List<CorrectorEntry<?>> getCorrectorEntries()
	{
		return correctorEntries;
	}

	public <T> void registerCorrector(Class<T> beanType, Corrector<? super T> corrector)
	{
		correctorEntries.add(new CorrectorEntry<T>(beanType, corrector));
	}

	public boolean unregisterCorrector(Corrector<?> corrector)
	{
		for (CorrectorEntry<?> entry : correctorEntries)
		{
			if (entry.corrector.equals(corrector))
			{
				correctorEntries.remove(entry);
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
		List<Correction> corrections = new ArrayList<>();
		// keep track which beans were already corrected
		// to not end in an infinite loop because two beans had a bidirectional relationship
		IdentityHashMap<Object, Boolean> alreadyCorrectedBeans = new IdentityHashMap<>();

		Queue<Object> beansToCorrect = new ArrayDeque<>();
		beansToCorrect.add(bean);
		Object beanToCorrect;
		while ((beanToCorrect = beansToCorrect.poll()) != null)
		{
			correctBean(beanToCorrect, corrections);
			alreadyCorrectedBeans.put(beanToCorrect, Boolean.TRUE);
			addNestedBeans(beanToCorrect, beansToCorrect, alreadyCorrectedBeans);
		}
		return corrections;
	}

	@SuppressWarnings("unchecked")
	private <T> void correctBean(T bean, List<Correction> corrections)
	{
		for (CorrectorEntry<?> entry : correctorEntries)
		{
			if (entry.beanType.isAssignableFrom(bean.getClass()))
			{
				Corrector<? super T> corrector = (Corrector<? super T>) entry.corrector;
				corrector.correct(bean, corrections);
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
		return MoreObjects.toStringHelper(TypeBasedCorrectionService.class).add("domain", domain).toString();
	}

	public static final class CorrectorEntry<T>
	{
		private final Class<T>				beanType;
		private final Corrector<? super T>	corrector;

		private CorrectorEntry(Class<T> beanType, Corrector<? super T> corrector)
		{
			this.beanType = Objects.requireNonNull(beanType, "beanType");
			this.corrector = Objects.requireNonNull(corrector, "corrector");
		}

		public Class<T> getBeanType()
		{
			return beanType;
		}

		public Corrector<? super T> getCorrector()
		{
			return corrector;
		}

		@Override
		public String toString()
		{
			return MoreObjects.toStringHelper(CorrectorEntry.class).add("beanType", beanType).add("corrector", corrector).toString();
		}
	}
}
