package de.subcentral.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;

public interface SettingsProperty<T, P extends Property<T>>
{
	public P originalProperty();

	public T getOriginal();

	public void setOriginal(T value);

	public P currentProperty();

	public T getCurrent();

	public void setCurrent(T value);

	public BooleanBinding changedBinding();

	public boolean hasChanged();

	public void reset();

	public void load(XMLConfiguration cfg);

	public void save(XMLConfiguration cfg);
}
