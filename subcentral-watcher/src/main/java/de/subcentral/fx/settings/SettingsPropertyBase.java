package de.subcentral.fx.settings;

import java.util.Objects;

import javafx.beans.property.Property;

public abstract class SettingsPropertyBase<T, P extends Property<T>> implements SettingsProperty<T, P>
{
	protected final String key;

	public SettingsPropertyBase(String key)
	{
		this.key = Objects.requireNonNull(key, "key");
	}

	public String getKey()
	{
		return key;
	}
}
