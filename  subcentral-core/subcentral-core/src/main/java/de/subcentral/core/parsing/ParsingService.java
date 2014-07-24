package de.subcentral.core.parsing;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import de.subcentral.core.util.SimplePropDescriptor;

public interface ParsingService
{
	public String getDomain();

	public default Object parse(String name)
	{
		return parse(name, ImmutableMap.of());
	}

	public default <T> T parse(String name, Class<T> type)
	{
		return parse(name, type, ImmutableMap.of());
	}

	public Object parse(String name, Map<SimplePropDescriptor, String> additionalInfo);

	public <T> T parse(String name, Class<T> type, Map<SimplePropDescriptor, String> additionalInfo);
}
