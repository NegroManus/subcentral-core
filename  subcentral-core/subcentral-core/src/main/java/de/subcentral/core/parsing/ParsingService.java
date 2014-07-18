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

	public Object parse(String name, Map<SimplePropDescriptor, String> additionalInfo);
}
