package de.subcentral.fx.settings;

import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public abstract class SettableBase implements Settable
{
	@Override
	public void load(URL file) throws ConfigurationException
	{
		XMLConfiguration cfg = ConfigurationHelper.load(file);
		load(cfg);
	}

	@Override
	public void load(Path file) throws ConfigurationException
	{
		XMLConfiguration cfg = ConfigurationHelper.load(file);
		load(cfg);
	}

	@Override
	public void save(Path file) throws ConfigurationException
	{
		XMLConfiguration cfg = new XMLConfiguration();
		save(cfg);
		ConfigurationHelper.save(cfg, file);
	}
}
