package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveMapper<T> implements Mapper<T>
{
	private final Class<T>	entityType;

	public ReflectiveMapper(Class<T> entityType)
	{
		this.entityType = Objects.requireNonNull(entityType, "entityType");
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropFromStringService propParsingService)
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
			throw new MappingException(props, entityType, e);
		}
	}
}
