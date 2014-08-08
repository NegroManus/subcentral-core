package de.subcentral.core.parsing;

import java.util.Map;

import de.subcentral.core.util.SimplePropDescriptor;

public interface Mapper<T>
{
	public T map(Map<SimplePropDescriptor, String> props);
}
