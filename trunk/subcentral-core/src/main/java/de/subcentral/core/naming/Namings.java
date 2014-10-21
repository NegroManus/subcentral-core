package de.subcentral.core.naming;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

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

	public static final List<String> nameEach(Collection<?> candidates, NamingService namingService, Map<String, Object> parameters)
	{
		if (candidates.isEmpty())
		{
			return ImmutableList.of();
		}
		ImmutableList.Builder<String> names = ImmutableList.builder();
		for (Object o : candidates)
		{
			names.add(namingService.name(o, parameters));
		}
		return names.build();
	}

	public static final String nameAll(Collection<?> candidates, NamingService namingService, Map<String, Object> parameters, String separator)
	{
		if (candidates.isEmpty())
		{
			return "";
		}
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
