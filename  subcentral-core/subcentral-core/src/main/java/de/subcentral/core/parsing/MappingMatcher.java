package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropertyDescriptor;

public interface MappingMatcher
{
	public Map<SimplePropertyDescriptor, String> map(String input);
}
