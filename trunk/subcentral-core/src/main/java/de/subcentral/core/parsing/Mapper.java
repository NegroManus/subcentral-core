package de.subcentral.core.parsing;

import java.util.Map;
import java.util.function.BiFunction;

import de.subcentral.core.util.SimplePropDescriptor;

public interface Mapper<T> extends BiFunction<Map<SimplePropDescriptor, String>, PropParsingService, T>
{
	public T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService) throws MappingException;

	@Override
	public default T apply(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService)
	{
		return map(props, propParsingService);
	}
}
