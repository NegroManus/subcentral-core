package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public interface MappingService
{
	public String getDomain();

	public <T> T map(Map<SimplePropDescriptor, String> info, Class<T> typeClass);
}
