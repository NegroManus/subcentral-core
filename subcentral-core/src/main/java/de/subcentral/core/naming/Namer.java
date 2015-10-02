package de.subcentral.core.naming;

import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;

public interface Namer<T> extends Function<T, String>
{
	public default String name(T obj) throws NamingException
	{
		return name(obj, ImmutableMap.of());
	}

	/**
	 * 
	 * @param obj
	 *            The object to name. Can be null.
	 * @param parameters
	 *            The parameters for this naming. Not null, can be empty.
	 * @return The generated name of the candidate or "" if the candidate was null.
	 * @throws NamingException
	 *             If an Exception occurs while naming. This Exception will be the {@link Exception#getCause() cause} of the thrown NamingException.
	 */
	public String name(T obj, Map<String, Object> parameters) throws NamingException;

	@Override
	public default String apply(T obj)
	{
		return name(obj);
	}
}
