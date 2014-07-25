package de.subcentral.core.parsing;

import java.util.HashMap;
import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public class MappingServiceImpl implements MappingService
{
	private String						domain;
	private Map<Class<?>, Mapper<?>>	mappers	= new HashMap<>();

	@Override
	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}

	public Map<Class<?>, Mapper<?>> getMappers()
	{
		return mappers;
	}

	public void setMappers(Map<Class<?>, Mapper<?>> mappers)
	{
		checkMappersMap(mappers);
		this.mappers = mappers;
	}

	public Mapper<?> registerMapper(Mapper<?> mapper)
	{
		return this.mappers.put(mapper.getType(), mapper);
	}

	public Mapper<?> unregisterNamer(Class<?> typeClass)
	{
		return mappers.remove(typeClass);
	}

	@Override
	public <T> T map(Map<SimplePropDescriptor, String> info, Class<T> typeClass)
	{
		@SuppressWarnings("unchecked")
		Mapper<T> mapper = (Mapper<T>) mappers.get(typeClass);
		if (mapper != null)
		{
			return mapper.map(info);
		}
		return null;
	}

	private void checkMappersMap(Map<Class<?>, Mapper<?>> mappers) throws IllegalArgumentException
	{
		for (Map.Entry<Class<?>, Mapper<?>> entry : mappers.entrySet())
		{
			if (!entry.getKey().equals(entry.getValue().getType()))
			{
				throw new IllegalArgumentException("The map contains an invalid entry: The class (" + entry.getKey()
						+ ") is not equal to the mapper's type " + entry.getValue().getType() + ".");
			}
		}
	}
}
