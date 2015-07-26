package de.subcentral.core.naming;

import de.subcentral.core.util.SimplePropDescriptor;

public interface PropToStringService
{
	public String convert(SimplePropDescriptor propDescriptor, Object prop);
}
