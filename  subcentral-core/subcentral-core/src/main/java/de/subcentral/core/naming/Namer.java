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

	/**
	 * 
	 * @param candidate
	 *            The object to name. Can be null.
	 * @param namingService
	 *            The NamingService to name objects that are part of the candidate. Can be null if not needed (depends on the Namer implementation).
	 * @param parameters
	 *            The parameters for this naming. Never null, can be empty.
	 * @return The generated name of the candidate or null if the candidate was null.
	 * @throws NamingException
	 *             If an Exception occurs while naming. This Exception will be the {@link Exception#getCause() cause} of the thrown NamingException.
	 */
	public String name(T candidate, NamingService namingService, Map<String, Object> parameters) throws NamingException;
}
