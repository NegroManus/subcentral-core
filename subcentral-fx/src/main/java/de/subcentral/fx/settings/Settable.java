package de.subcentral.fx.settings;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface Settable extends Observable
{
	public ReadOnlyBooleanProperty changedProperty();

	public boolean changed();

	public default void load(ImmutableConfiguration cfg)
	{
		load(cfg, true);
	}

	public void load(ImmutableConfiguration cfg, boolean resetChanged);

	public default void save(Configuration cfg)
	{
		save(cfg, true);
	}

	public void save(Configuration cfg, boolean resetChanged);

	public default void load(URL file) throws ConfigurationException
	{
		load(file, true);
	}

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(URL file, boolean resetChanged) throws ConfigurationException;

	public default void load(Path file) throws ConfigurationException
	{
		load(file, true);
	}

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(Path file, boolean resetChanged) throws ConfigurationException;

	public default void save(Path file) throws ConfigurationException
	{
		save(file, true);
	}

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void save(Path file, boolean resetChanged) throws ConfigurationException;
}
