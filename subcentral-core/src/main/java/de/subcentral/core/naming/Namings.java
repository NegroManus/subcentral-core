package de.subcentral.core.naming;

import java.util.Map;

public class Namings
{
	public static final <T> T readParameter(Map<String, Object> parameters, String key, Class<T> valueClass, T defaultValue)
	{
		return valueClass.cast(parameters.getOrDefault(parameters.get(key), defaultValue));
	}

	private Namings()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore should not be instantiated.");
	}
}
