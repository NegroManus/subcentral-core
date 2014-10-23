package de.subcentral.core.naming;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

public class ConditionalNamingService implements NamingService
{
	private final String					domain;
	private final List<ConditionalNamer<?>>	namers				= new CopyOnWriteArrayList<>();
	private UnaryOperator<String>			wholeNameOperator	= UnaryOperator.identity();

	public ConditionalNamingService(String domain)
	{
		this.domain = Objects.requireNonNull(domain, "domain");
	}

	@Override
	public String getDomain()
	{
		return domain;
	}

	public List<ConditionalNamer<?>> getNamers()
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
		for (ConditionalNamer<?> namer : namers)
		{
			if (namer.test(candidate))
			{
				return (Namer<? super T>) namer;
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
