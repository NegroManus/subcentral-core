package de.subcentral.core.name;

import de.subcentral.core.util.SimplePropDescriptor;

public interface PropToStringService
{
	public String convert(SimplePropDescriptor propDescriptor, Object prop);
}
