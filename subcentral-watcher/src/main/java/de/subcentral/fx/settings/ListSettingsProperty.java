package de.subcentral.fx.settings;

import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListSettingsProperty<E> extends ObjectSettingsPropertyBase<ObservableList<E>, ListProperty<E>>
{
	public ListSettingsProperty(String key, BiFunction<XMLConfiguration, String, ObservableList<E>> loader, TriConsumer<XMLConfiguration, String, ObservableList<E>> saver)
	{
		super(key, FXCollections.observableArrayList(), loader, saver);
	}

	@Override
	protected ListProperty<E> createProperty(Object bean, String name, ObservableList<E> initialValue)
	{
		return new SimpleListProperty<>(bean, name, initialValue);
	}
}
