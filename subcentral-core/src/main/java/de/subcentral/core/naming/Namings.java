package de.subcentral.core.naming;

import java.util.Map;

public class Namings
{
	public static final <T> T readParameter(Map<String, Object> parameters, String key, Class<T> valueClass, T defaultValue)
	{
		if (parameters.containsKey(key))
		{
			return valueClass.cast(parameters.get(key));
		}
		return defaultValue;
	}

	private Namings()
	{
		// utility class
	}
}
