package de.subcentral.fx.settings;

import org.apache.commons.configuration2.XMLConfiguration;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;

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

	public BooleanBinding changedBinding();

	public default boolean hasChanged()
	{
		return changedBinding().get();
	}

	public default void reset()
	{
		setCurrent(getOriginal());
	}

	public void load(XMLConfiguration cfg);

	public void save(XMLConfiguration cfg);
}
