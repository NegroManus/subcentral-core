package de.subcentral.fx.settings;

import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ObjectSettingsProperty<T> extends ObjectSettingsPropertyBase<T, Property<T>>
{
	public ObjectSettingsProperty(String key, T defaultValue, BiFunction<XMLConfiguration, String, T> loader, TriConsumer<XMLConfiguration, String, T> saver)
	{
		super(key, defaultValue, loader, saver);
	}

	@Override
	protected Property<T> createProperty(Object bean, String name, T initialValue)
	{
		return new SimpleObjectProperty<>(bean, name, initialValue);
	}
}
