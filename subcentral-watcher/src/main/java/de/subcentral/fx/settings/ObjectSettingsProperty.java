package de.subcentral.fx.settings;

import java.util.function.Function;

import de.subcentral.fx.FxUtil;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ObjectSettingsProperty<T> extends ObjectSettingsPropertyBase<T, Property<T>>
{
	public ObjectSettingsProperty(String key, T defaultValue, ConfigurationPropertyHandler<T> handler)
	{
		this(key, defaultValue, null, handler);
	}

	public ObjectSettingsProperty(String key, T defaultValue, Function<T, Observable[]> propertiesExtractor, ConfigurationPropertyHandler<T> handler)
	{
		super(key, defaultValue, observablePropertyCreator(propertiesExtractor), handler);
	}

	private static <T> Function<Property<T>, Observable> observablePropertyCreator(Function<T, Observable[]> propertiesExtractor)
	{
		return (Property<T> p) -> propertiesExtractor == null ? p : FxUtil.observeBean(p, propertiesExtractor);
	}

	@Override
	protected Property<T> createProperty(Object bean, String name, T initialValue)
	{
		return new SimpleObjectProperty<>(bean, name, initialValue);
	}
}
