package de.subcentral.core.naming;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import de.subcentral.core.util.StringUtil;

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
		StringBuilder name = new StringBuilder();
		for (Object candidate : candidates)
		{
			name.append(namingService.name(candidate, parameters));
			name.append(separator);
		}
		return StringUtil.stripEnd(name, separator).toString();
	}

	private Namings()
	{
		// utility class
	}
}
