package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public interface MappingMatcher
{
	public Map<SimplePropDescriptor, String> map(String input);
}
