package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public class SimpleMappingService implements MappingService
{
	private Map<Class<?>, Mapper<?>>	mappers	= new HashMap<>(0);

	public Map<Class<?>, Mapper<?>> getMappers()
	{
		return mappers;
	}

	public void setMappers(Map<Class<?>, Mapper<?>> mappers)
	{
		Objects.requireNonNull(mappers);
		this.mappers = mappers;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService, Class<T> entityType)
	{
		Mapper<T> m = (Mapper<T>) mappers.get(entityType);
		if (m != null)
		{
			return m.map(props, propParsingService);
		}
		throw new MappingException(props, entityType, "No mapper registered for entity type ");
	}
}
