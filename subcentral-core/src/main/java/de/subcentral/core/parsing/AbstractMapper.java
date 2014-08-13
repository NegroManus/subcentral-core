package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Objects;

import de.subcentral.core.util.SimplePropDescriptor;

public abstract class AbstractMapper<T> implements Mapper<T>
{
	@Override
	public T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService) throws MappingException
	{
		try
		{
			Objects.requireNonNull(props, "props");
			Objects.requireNonNull(propParsingService, "propParsingService");
			return doMap(props, propParsingService);
		}
		catch (Exception e)
		{
			throw new MappingException("Mapping failed", e, props, null);
		}
	}

	protected abstract T doMap(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService);
}
