package de.subcentral.core.naming;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

public interface NamingService extends Function<Object, String>
{
	public String getDomain();

	public boolean canName(Object entity);

	public default <T> String name(T entity) throws NoNamerRegisteredException, NamingException
	{
		return name(entity, ImmutableMap.of());
	}

	public <T> String name(T entity, Map<String, Object> parameters) throws NoNamerRegisteredException, NamingException;

	@Override
	public default String apply(Object entity)
	{
		return name(entity);
	}
}
