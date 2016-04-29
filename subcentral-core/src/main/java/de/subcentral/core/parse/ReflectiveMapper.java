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

	public ReflectiveMapper(Class<T> beanType, ParsePropService parsePropService)
	{
		super(parsePropService);
		this.beanType = Objects.requireNonNull(beanType, "beanType");
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props)
	{
		try
		{
			return ParsingUtil.reflectiveMapping(beanType, props, parsePropService);
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Class<?> getTargetType()
	{
		return beanType;
	}
}
