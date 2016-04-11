package de.subcentral.fx.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StringSettingsProperty extends ObjectSettingsPropertyBase<String, StringProperty>
{
	public StringSettingsProperty(String key)
	{
		this(key, null);
	}

	public StringSettingsProperty(String key, String defaultValue)
	{
		super(key, ConfigurationPropertyHandlers.STRING_HANDLER, defaultValue, null);
	}

	@Override
	protected StringProperty createProperty(Object bean, String name, String initialValue)
	{
		return new SimpleStringProperty(bean, name, initialValue);
	}
}
