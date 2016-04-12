package de.subcentral.fx.settings;

import java.util.LinkedHashMap;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MapSettingsProperty<K, V> extends ObjectSettingsPropertyBase<ObservableMap<K, V>, MapProperty<K, V>>
{
	public MapSettingsProperty(String key, ConfigurationPropertyHandler<ObservableMap<K, V>> handler)
	{
		super(key, handler, FXCollections.observableMap(new LinkedHashMap<>()), null);
	}

	@Override
	protected MapProperty<K, V> createProperty(Object bean, String name, ObservableMap<K, V> initialValue)
	{
		return new SimpleMapProperty<>(bean, name, initialValue);
	}
}
