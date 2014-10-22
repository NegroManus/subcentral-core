package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

import org.apache.commons.lang3.ClassUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

public class ConditionalNamingService implements NamingService
{
	private final String										domain;
	private final ListMultimap<Class<?>, ConditionalNamer<?>>	namers				= ArrayListMultimap.create();
	private UnaryOperator<String>								wholeNameOperator	= UnaryOperator.identity();

	public ConditionalNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public ListMultimap<Class<?>, ConditionalNamer<?>> getNamers()
	{
		return namers;
	}

	@Override
	public boolean canName(Object candidate)
	{
		return getNamer(candidate) != null;
	}

	@SuppressWarnings("unchecked")
	public <T> Namer<? super T> getNamer(T candidate)
	{
		if (candidate == null)
		{
			return null;
		}
		Class<?> searchClass = candidate.getClass();
		while (searchClass != Object.class)
		{
			for (ConditionalNamer<?> conditionalNamer : namers.get(searchClass))
			{
				ConditionalNamer<? super T> castedNamer = (ConditionalNamer<? super T>) conditionalNamer;
				if (castedNamer.test((T) candidate))
				{
					return castedNamer;
				}
			}
			searchClass = searchClass.getSuperclass();
		}
		for (Class<?> interfaceClass : ClassUtils.getAllInterfaces(candidate.getClass()))
		{
			for (ConditionalNamer<?> conditionalNamer : namers.get(interfaceClass))
			{
				ConditionalNamer<? super T> castedNamer = (ConditionalNamer<? super T>) conditionalNamer;
				if (castedNamer.test((T) candidate))
				{
					return castedNamer;
				}
			}
		}
		return null;
	}

	@Override
	public String name(Object candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		return doName(candidate, parameters);
	}

	private final <T> String doName(T candidate, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException
	{
		Namer<? super T> namer = getNamer(candidate);
		if (namer != null)
		{
			return wholeNameOperator.apply(namer.name(candidate, parameters));
		}
		throw new NoNamerRegisteredException(candidate);
	}
}
