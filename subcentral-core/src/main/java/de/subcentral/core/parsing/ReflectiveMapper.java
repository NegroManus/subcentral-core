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
	try
	{
	    T bean = beanType.newInstance();
	    ParsingUtil.reflectiveMapping(bean, props, propParsingService);
	    return bean;
	}
	catch (InstantiationException | IllegalAccessException e)
	{
	    throw new MappingException(props, beanType, e);
	}
    }
}
