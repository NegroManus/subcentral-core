package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class ReflectiveMapper<T> implements Mapper<T>
{
	private final Class<T>				entityType;
	private final PropParsingService	pps;

	public ReflectiveMapper(Class<T> entityType, PropParsingService pps)
	{
		this.entityType = entityType;
		this.pps = pps;
	}

	@Override
	public T map(Map<SimplePropDescriptor, String> props)
	{
		T entity;
		try
		{
			entity = entityType.newInstance();
			Parsings.reflectiveMapping(entity, props, pps);
			return entity;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			throw new ParsingException(e);
		}
	}
}
