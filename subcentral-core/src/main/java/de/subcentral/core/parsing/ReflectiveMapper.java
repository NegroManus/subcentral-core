package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveMapper<T> implements Mapper<T>
{
	private final Class<T> beanType;

	public ReflectiveMapper(Class<T> beanType)
	{
		this.beanType = Objects.requireNonNull(beanType, "beanType");
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropFromStringService propParsingService)
	{
		return ParsingUtil.reflectiveMapping(beanType, props, propParsingService);
	}
}
