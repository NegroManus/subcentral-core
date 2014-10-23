package de.subcentral.core.naming;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class ConditionalNamer<T> implements Namer<T>, Predicate<Object>
{
	private final Namer<T>			namer;
	private final Predicate<Object>	condition;

	private ConditionalNamer(Namer<T> namer, Predicate<Object> condition)
	{
		this.namer = Objects.requireNonNull(namer, "namer");
		this.condition = Objects.requireNonNull(condition, "condition");
	}

	@Override
	public boolean test(Object candidate)
	{
		return condition.test(candidate);
	}

	@Override
	public String name(T candidate, Map<String, Object> parameters) throws NamingException
	{
		if (condition.test(candidate))
		{
			return namer.name(candidate, parameters);
		}
		return null;
	}
}
