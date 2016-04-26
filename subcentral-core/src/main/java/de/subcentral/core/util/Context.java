package de.subcentral.core.util;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class Context
{
	private Map<String, Object> data = ImmutableMap.of();

	public <T> T get(String key, Class<T> type)
	{
		Object value = data.get(key);
		if (type.isInstance(value))
		{
			return type.cast(value);
		}
		return null;
	}

	public <T> T get(String key, Class<T> type, T defaultValue)
	{
		Object value = data.getOrDefault(key, defaultValue);
		if (type.isInstance(value))
		{
			return type.cast(value);
		}
		return null;
	}

	public Object set(String key, Object value)
	{
		if (data instanceof ImmutableMap)
		{
			data = new HashMap<>();
		}
		return data.put(key, value);
	}
}
