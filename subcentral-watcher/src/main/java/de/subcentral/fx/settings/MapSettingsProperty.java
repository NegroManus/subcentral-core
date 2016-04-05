package de.subcentral.fx.settings;

import java.util.LinkedHashMap;
import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MapSettingsProperty<K, V> extends ObjectSettingsPropertyBase<ObservableMap<K, V>, MapProperty<K, V>>
{
	public MapSettingsProperty(String key, BiFunction<XMLConfiguration, String, ObservableMap<K, V>> loader, TriConsumer<XMLConfiguration, String, ObservableMap<K, V>> saver)
	{
		super(key, FXCollections.observableMap(new LinkedHashMap<>()), loader, saver);
	}

	@Override
	protected MapProperty<K, V> createProperty(Object bean, String name, ObservableMap<K, V> initialValue)
	{
		return new SimpleMapProperty<>(bean, name, initialValue);
	}
}
