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

	public default boolean changed()
	{
		return changedProperty().get();
	}

	public void reset();

	public void load(ImmutableConfiguration cfg);

	public void save(Configuration cfg);

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(URL file) throws ConfigurationException;

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 */
	public void load(Path file) throws ConfigurationException;

	/**
	 * Must be called in the JavaFX Application thread.
	 * 
	 * @param file
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public void save(Path file) throws ConfigurationException;
}
