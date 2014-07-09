package de.subcentral.core.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;

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

	public static final String name(Iterable<?> candidates, NamingService namingService, Map<String, Object> parameters, String separator)
	{
		List<String> names = new ArrayList<>();
		for (Object candidate : candidates)
		{
			names.add(namingService.name(candidate, parameters));
		}
		return Joiner.on(separator).join(names);
	}

	private Namings()
	{
		// utility class
	}
}
