package de.subcentral.fx.settings;

import javafx.beans.property.Property;

public interface SettingsProperty<T, P extends Property<T>> extends Settable
{
	public String getKey();

	public P property();

	public T getValue();

	public void setValue(T value);
}
