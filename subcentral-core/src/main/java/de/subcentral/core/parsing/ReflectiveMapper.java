package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveMapper<T> implements Mapper<T>
{
	private final Class<T>	entityType;

	public ReflectiveMapper(Class<T> entityType)
	{
		this.entityType = entityType;
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		T entity;
		try
		{
			entity = entityType.newInstance();
			Parsings.reflectiveMapping(entity, props, propParsingService);
			return entity;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new ParsingException(e);
		}
	}
}
