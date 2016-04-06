package de.subcentral.fx.settings;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;

public interface ConfigurationPropertyHandler<T>
{
	public T load(ImmutableConfiguration cfg, String key, T defaultValue);

	public void save(Configuration cfg, String key, T value);
}
