package de.subcentral.core.correction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

/**
 * 
 * @implSpec #thread-safe
 *
 */
public class TypeBasedCorrectionService implements CorrectionService
{
	private final String												domain;
	private final ListMultimap<Class<?>, Corrector<?>>					correctors				= Multimaps.synchronizedListMultimap(LinkedListMultimap.create());
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

	public ListMultimap<Class<?>, Corrector<?>> getCorrectors()
	{
		return Multimaps.unmodifiableListMultimap(correctors);
	}

	public Map<Class<?>, Function<?, List<? extends Object>>> getNestedBeansRetrievers()
	{
		return Collections.unmodifiableMap(nestedBeansRetrievers);
	}

	public <T> void registerCorrector(Class<T> beanType, Corrector<? super T> corrector)
	{
		correctors.put(beanType, corrector);
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
		for (Corrector<?> c : correctors.get(bean.getClass()))
		{
			Corrector<? super T> corrector = (Corrector<? super T>) c;
			corrector.correct(bean, corrections);
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
}
