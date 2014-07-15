package de.subcentral.core.parsing;

import java.util.Map;
import java.util.Set;

import de.subcentral.core.util.SimplePropertyDescriptor;

public interface Mapper<T>
{
	public Class<T> getType();

	public Set<SimplePropertyDescriptor> getKnownProperties();

	public T map(Map<SimplePropertyDescriptor, String> info);
}
