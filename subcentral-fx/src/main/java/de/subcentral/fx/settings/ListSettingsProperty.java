package de.subcentral.fx.settings;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListSettingsProperty<E> extends ObjectSettingsPropertyBase<ObservableList<E>, ListProperty<E>>
{
	public ListSettingsProperty(String key, ConfigurationPropertyHandler<ObservableList<E>> handler)
	{
		this(key, handler, FXCollections.observableArrayList());
	}

	public ListSettingsProperty(String key, ConfigurationPropertyHandler<ObservableList<E>> handler, ObservableList<E> initialValue)
	{
		super(key, (Object bean, String name) -> new SimpleListProperty<>(bean, name, initialValue), handler);
	}
}
