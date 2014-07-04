package de.subcentral.core.naming;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface NamingService
{
	public String getDomain();

	public boolean canName(Object candidate);

	public default <T> String name(T candidate) throws NamingException
	{
		return name(candidate, ImmutableMap.of());
	}

	public <T> String name(T candidate, Map<String, Object> parameters) throws NamingException;
}
