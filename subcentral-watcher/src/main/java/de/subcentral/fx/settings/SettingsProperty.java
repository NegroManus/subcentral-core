package de.subcentral.fx.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;

public interface SettingsProperty<T, P extends Property<T>>
{
	public T getOriginal();

	public P currentProperty();

	public default T getCurrent()
	{
		return currentProperty().getValue();
	}

	public default void setCurrent(T value)
	{
		currentProperty().setValue(value);
	}

	public ReadOnlyBooleanProperty changedProperty();

	public default boolean hasChanged()
	{
		return changedProperty().get();
	}

	public default void reset()
	{
		setCurrent(getOriginal());
	}

	public void load(XMLConfiguration cfg);

	public void save(XMLConfiguration cfg);
}
