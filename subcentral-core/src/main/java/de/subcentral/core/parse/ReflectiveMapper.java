package de.subcentral.core.parse;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveMapper<T> extends AbstractMapper<T>
{
	private final Class<T> beanType;

	public ReflectiveMapper(Class<T> beanType)
	{
		this(beanType, ParsingDefaults.getDefaultPropFromStringService());
	}

	public ReflectiveMapper(Class<T> beanType, PropFromStringService propFromStringService)
	{
		super(propFromStringService);
		this.beanType = Objects.requireNonNull(beanType, "beanType");
	}

	@Override
	public T doMap(Map<SimplePropDescriptor, String> props)
	{
		return ParsingUtil.reflectiveMapping(beanType, props, propFromStringService);
	}

	@Override
	protected Class<?> getTargetType()
	{
		return beanType;
	}
}
