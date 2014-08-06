package de.subcentral.core.naming;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public interface NamingService
{
	public String getDomain();

	public boolean canName(Object entity);

	public default <T> String name(T entity) throws NamingException
	{
		return name(entity, ImmutableMap.of());
	}

	public <T> String name(T entity, Map<String, Object> parameters) throws NamingException;
}
