package de.subcentral.core.util;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

public class Context
{
	public static final Context			EMPTY	= new Context(ImmutableMap.of());

	private final Map<String, Object>	data;

	Context(Map<String, Object> data)
	{
		this.data = ImmutableMap.copyOf(data);
	}

	public static Context of(String key, Object value)
	{
		return new Context(ImmutableMap.of(key, value));
	}

	public static Context of(Map<String, Object> entries)
	{
		return new Context(entries);
	}

	public static Builder builder()
	{
		return new Builder();
	}

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
		Object value = data.get(key);
		if (type.isInstance(value))
		{
			return type.cast(value);
		}
		return defaultValue;
	}

	public Boolean getBoolean(String key)
	{
		return get(key, Boolean.class);
	}

	public Boolean getBoolean(String key, Boolean defaultValue)
	{
		return get(key, Boolean.class, defaultValue);
	}

	public String getString(String key)
	{
		return get(key, String.class);
	}

	public String getString(String key, String defaultValue)
	{
		return get(key, String.class, defaultValue);
	}

	public Set<Map.Entry<String, Object>> entrySet()
	{
		return data.entrySet();
	}

	public static class Builder
	{
		private final ImmutableMap.Builder<String, Object> dataBuilder = ImmutableMap.builder();

		private Builder()
		{

		}

		public Builder set(String key, Object value)
		{
			dataBuilder.put(key, value);
			return this;
		}

		public Builder setAll(Context ctx)
		{
			dataBuilder.putAll(ctx.data);
			return this;
		}

		public Builder setAll(Map<String, Object> entries)
		{
			dataBuilder.putAll(entries);
			return this;
		}

		public Context build()
		{
			Map<String, Object> data = dataBuilder.build();
			if (data.isEmpty())
			{
				return Context.EMPTY;
			}
			return new Context(data);
		}
	}

}
