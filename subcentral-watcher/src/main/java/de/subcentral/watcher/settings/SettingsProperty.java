package de.subcentral.watcher.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

public interface SettingsProperty<T>
{
	public Property<T> getCurrent();

	public ReadOnlyProperty<T> getOriginal();

	public BooleanBinding changedBinding();

	public boolean hasChanged();

	public void reset();

	public void load(XMLConfiguration cfg);

	public void save(XMLConfiguration cfg);
}
