package de.subcentral.fx.settings;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;

public interface ConfigurationPropertyHandler<T> {
	public T get(ImmutableConfiguration cfg, String key);

	public void add(Configuration cfg, String key, T value);
}
