package de.subcentral.core.naming;

import java.util.Map;

public class Namings
{
	public static final <T> T readParameter(Map<String, Object> namingSettings, String key, Class<T> valueClass, T defaultValue)
	{
		if (namingSettings.containsKey(key))
		{
			return valueClass.cast(namingSettings.get(key));
		}
		return defaultValue;
	}

	private Namings()
	{
		// utility class
	}
}
