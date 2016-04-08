package de.subcentral.fx.settings;

import javafx.beans.property.Property;

public interface SettingsProperty<T, P extends Property<T>> extends Settable
{
	public String getKey();

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

	@Override
	public default void reset()
	{
		setCurrent(getOriginal());
	}
}
