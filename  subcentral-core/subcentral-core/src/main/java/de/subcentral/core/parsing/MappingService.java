package de.subcentral.core.parsing;

import java.util.Map;

public interface MappingService
{
	public String getDomain();

	public <T> T map(Map<String, String> info, Class<T> typeClass);
}
