package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMapper<T> implements Mapper<T>
{
	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService) throws MappingException
	{
		try
		{
			return doMap(props, propFromStringService);
		}
		catch (Exception e)
		{
			throw new MappingException(props, null, "Exception while mapping", e);
		}
	}

	protected abstract T doMap(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService);
}
