package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import de.subcentral.core.util.SimplePropDescriptor;

public class ConditionalMapper<T> implements Mapper<T>
{
	private final Predicate<Map<SimplePropDescriptor, String>>	condition;
	private final Mapper<? extends T>							mapper;

	public ConditionalMapper(Predicate<Map<SimplePropDescriptor, String>> condition, Mapper<? extends T> mapper)
	{
		Objects.requireNonNull(condition, "condition");
		Objects.requireNonNull(mapper, "mapper");
		this.condition = condition;
		this.mapper = mapper;
	}

	public Predicate<Map<SimplePropDescriptor, String>> getCondition()
	{
		return condition;
	}

	public Mapper<? extends T> getMapper()
	{
		return mapper;
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService) throws MappingException
	{
		if (condition.test(props))
		{
			return mapper.map(props, propParsingService);
		}
		return null;
	}
}
