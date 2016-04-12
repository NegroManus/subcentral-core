package de.subcentral.fx.settings;

import java.util.function.Function;

import de.subcentral.fx.FxUtil;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ObjectSettingsProperty<T> extends ObjectSettingsPropertyBase<T, Property<T>>
{
	public ObjectSettingsProperty(String key, ConfigurationPropertyHandler<T> handler)
	{
		this(key, handler, null, null);
	}

	public ObjectSettingsProperty(String key, ConfigurationPropertyHandler<T> handler, T defaultValue)
	{
		this(key, handler, defaultValue, null);
	}

	public ObjectSettingsProperty(String key, ConfigurationPropertyHandler<T> handler, T defaultValue, Function<T, Observable[]> propertiesExtractor)
	{
		super(key, handler, defaultValue, observablePropertyCreator(propertiesExtractor));
	}

	private static <T> Function<Property<T>, Observable> observablePropertyCreator(Function<T, Observable[]> propertiesExtractor)
	{
		return propertiesExtractor == null ? null : (Property<T> p) -> FxUtil.observeBean(p, propertiesExtractor);
	}

	@Override
	protected Property<T> createProperty(Object bean, String name, T initialValue)
	{
		return new SimpleObjectProperty<>(bean, name, initialValue);
	}
}
