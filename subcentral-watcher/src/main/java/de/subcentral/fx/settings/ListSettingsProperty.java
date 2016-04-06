package de.subcentral.fx.settings;

import java.util.function.Function;

import de.subcentral.fx.FxUtil;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListSettingsProperty<E> extends ObjectSettingsPropertyBase<ObservableList<E>, ListProperty<E>>
{
	public ListSettingsProperty(String key, ConfigurationPropertyHandler<ObservableList<E>> handler)
	{
		this(key, null, handler);
	}

	public ListSettingsProperty(String key, Function<E, Observable[]> propertiesExtractor, ConfigurationPropertyHandler<ObservableList<E>> handler)
	{
		super(key, FXCollections.observableArrayList(), propertyObservableCreator(propertiesExtractor), handler);
	}

	private static <E> Function<ListProperty<E>, Observable> propertyObservableCreator(Function<E, Observable[]> propertiesExtractor)
	{
		return (ListProperty<E> p) -> propertiesExtractor == null ? p : FxUtil.observeBeanList(p, propertiesExtractor);
	}

	@Override
	protected ListProperty<E> createProperty(Object bean, String name, ObservableList<E> initialValue)
	{
		return new SimpleListProperty<>(bean, name, initialValue);
	}
}
