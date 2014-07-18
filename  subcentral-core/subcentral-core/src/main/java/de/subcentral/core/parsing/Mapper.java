package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Set;

import de.subcentral.core.util.SimplePropDescriptor;

public interface Mapper<T>
{
	public Class<T> getType();

	public Set<SimplePropDescriptor> getKnownProperties();

	public T map(Map<SimplePropDescriptor, String> info);
}
