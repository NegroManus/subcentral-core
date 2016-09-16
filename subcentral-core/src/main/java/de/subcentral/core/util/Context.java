package de.subcentral.core.util;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

public abstract class Context {
	public static final Context EMPTY = new EmptyContext();

	public static Context of(String key, Object value) {
		return new MapContext(ImmutableMap.of(key, value));
	}

	public static Context of(Map<String, Object> entries) {
		return new MapContext(entries);
	}

	public static Builder builder() {
		return new Builder();
	}

	public abstract <T> T get(String key, Class<T> type);

	public abstract <T> T get(String key, Class<T> type, T defaultValue);

	public abstract Boolean getBoolean(String key);

	public abstract Boolean getBoolean(String key, Boolean defaultValue);

	public abstract String getString(String key);

	public abstract String getString(String key, String defaultValue);

	public abstract Map<String, Object> asMap();

	private static class MapContext extends Context {
		private final Map<String, Object> data;

		MapContext(Map<String, Object> data) {
			this.data = ImmutableMap.copyOf(data);
		}

		@Override
		public <T> T get(String key, Class<T> type) {
			Object value = data.get(key);
			if (type.isInstance(value)) {
				return type.cast(value);
			}
			return null;
		}

		@Override
		public <T> T get(String key, Class<T> type, T defaultValue) {
			Object value = data.get(key);
			if (type.isInstance(value)) {
				return type.cast(value);
			}
			return defaultValue;
		}

		@Override
		public Boolean getBoolean(String key) {
			return get(key, Boolean.class);
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			return get(key, Boolean.class, defaultValue);
		}

		@Override
		public String getString(String key) {
			return get(key, String.class);
		}

		@Override
		public String getString(String key, String defaultValue) {
			return get(key, String.class, defaultValue);
		}

		@Override
		public Map<String, Object> asMap() {
			return data;
		}
	}

	private static class EmptyContext extends Context {
		@Override
		public <T> T get(String key, Class<T> type) {
			return null;
		}

		@Override
		public <T> T get(String key, Class<T> type, T defaultValue) {
			return defaultValue;
		}

		@Override
		public Boolean getBoolean(String key) {
			return null;
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			return defaultValue;
		}

		@Override
		public String getString(String key) {
			return null;
		}

		@Override
		public String getString(String key, String defaultValue) {
			return defaultValue;
		}

		@Override
		public Map<String, Object> asMap() {
			return ImmutableMap.of();
		}
	}

	public static class Builder {
		private final ImmutableMap.Builder<String, Object> dataBuilder = ImmutableMap.builder();

		private Builder() {

		}

		public Builder set(String key, Object value) {
			dataBuilder.put(key, value);
			return this;
		}

		public Builder setAll(Context ctx) {
			dataBuilder.putAll(ctx.asMap());
			return this;
		}

		public Builder setAll(Map<String, Object> entries) {
			dataBuilder.putAll(entries);
			return this;
		}

		public Context build() {
			Map<String, Object> data = dataBuilder.build();
			if (data.isEmpty()) {
				return Context.EMPTY;
			}
			return new MapContext(data);
		}
	}
}
