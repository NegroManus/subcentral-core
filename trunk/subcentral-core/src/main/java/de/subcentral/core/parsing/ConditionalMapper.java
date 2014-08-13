package de.subcentral.core.parsing;

import java.util.Map;
import java.util.function.Predicate;

import de.subcentral.core.util.SimplePropDescriptor;

public class ConditionalMapper<T> implements Mapper<T>
{
	private final Predicate<Map<SimplePropDescriptor, String>>	condition;
	private final Mapper<T>										mapper;

	public ConditionalMapper(Predicate<Map<SimplePropDescriptor, String>> condition, Mapper<T> mapper)
	{
		this.condition = condition;
		this.mapper = mapper;
	}

	public Predicate<Map<SimplePropDescriptor, String>> getCondition()
	{
		return condition;
	}

	public Mapper<T> getMapper()
	{
		return mapper;
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props) throws MappingException
	{
		if (condition.test(props))
		{
			return mapper.map(props);
		}
		throw new MappingException("Could not map: Condition tested false");
	}
}
