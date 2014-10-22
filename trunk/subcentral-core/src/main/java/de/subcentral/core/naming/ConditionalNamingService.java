package de.subcentral.core.naming;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ConditionalNamingService implements NamingService
{
	private final String							domain;
	private final Map<Predicate<Object>, Namer<?>>	namers				= new HashMap<>(8);
	private UnaryOperator<String>					wholeNameOperator	= UnaryOperator.identity();

	public ConditionalNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public Map<Predicate<Object>, Namer<?>> getNamers()
	{
		return namers;
	}

	@Override
	public boolean canName(Object candidate)
	{
		return getNamer(candidate) != null;
	}

	public Namer<?> getNamer(Object candidate)
	{
		if (candidate == null)
		{
			return null;
		}
		for (Map.Entry<Predicate<Object>, Namer<?>> entry : namers.entrySet())
		{
			if (entry.getKey().test(candidate))
			{
				return entry.getValue();
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
		@SuppressWarnings("unchecked")
		Namer<T> namer = (Namer<T>) getNamer(candidate);
		if (namer != null)
		{
			return wholeNameOperator.apply(namer.name(candidate, parameters));
		}
		throw new NoNamerRegisteredException(candidate);
	}
}
