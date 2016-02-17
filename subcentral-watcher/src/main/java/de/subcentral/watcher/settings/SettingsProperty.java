package de.subcentral.watcher.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.property.Property;

public abstract class SettingsProperty<T>
{
	public abstract Property<T> getProperty();

	public abstract void load(XMLConfiguration cfg);

	public abstract void save(XMLConfiguration cfg);
}
