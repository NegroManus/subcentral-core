package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropertyDescriptor;

public interface MappingService
{
	public String getDomain();

	public <T> T map(Map<SimplePropertyDescriptor, String> info, Class<T> typeClass);
}
