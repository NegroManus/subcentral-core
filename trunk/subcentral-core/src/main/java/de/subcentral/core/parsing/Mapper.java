package de.subcentral.core.parsing;

import java.util.Map;
import java.util.function.BiFunction;

import de.subcentral.core.util.SimplePropDescriptor;

public interface Mapper<T> extends BiFunction<Map<SimplePropDescriptor, String>, PropFromStringService, T>
{
	public T map(Map<SimplePropDescriptor, String> props, PropFromStringService propFromStringService) throws MappingException;

	@Override
	public default T apply(Map<SimplePropDescriptor, String> props, PropFromStringService propParsingService)
	{
		return map(props, propParsingService);
	}
}
