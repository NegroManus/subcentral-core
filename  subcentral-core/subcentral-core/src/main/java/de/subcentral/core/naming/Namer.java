package de.subcentral.core.naming;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface Namer<T>
{
	public Class<T> getType();

	public default String name(T candidate) throws NamingException
	{
		return name(candidate, null, ImmutableMap.of());
	}

	public default String name(T candidate, NamingService namingService) throws NamingException
	{
		return name(candidate, namingService, ImmutableMap.of());
	}

	public default String name(T candidate, Map<String, Object> parameters) throws NamingException
	{
		return name(candidate, null, parameters);
	}

	public String name(T candidate, NamingService namingService, Map<String, Object> parameters) throws NamingException;
}
