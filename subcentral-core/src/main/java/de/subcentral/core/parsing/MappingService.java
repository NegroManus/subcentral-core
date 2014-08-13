package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public interface MappingService
{
	public <T> T map(Map<SimplePropDescriptor, String> props, PropParsingService propParsingService, Class<T> entityType);
}
