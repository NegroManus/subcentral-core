package de.subcentral.fx.settings;

import java.util.LinkedHashMap;

import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public final class MapSettingsProperty<K, V> extends ObjectSettingsPropertyBase<ObservableMap<K, V>, MapProperty<K, V>> {
	public MapSettingsProperty(String key, ConfigurationPropertyHandler<ObservableMap<K, V>> handler) {
		this(key, handler, FXCollections.observableMap(new LinkedHashMap<>()));
	}

	public MapSettingsProperty(String key, ConfigurationPropertyHandler<ObservableMap<K, V>> handler, ObservableMap<K, V> initialValue) {
		super(key, (Object bean, String name) -> new SimpleMapProperty<K, V>(bean, name, initialValue), handler);
	}
}
