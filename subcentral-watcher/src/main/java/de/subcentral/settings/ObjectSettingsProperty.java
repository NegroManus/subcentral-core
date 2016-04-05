package de.subcentral.settings;

import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public class ObjectSettingsProperty<T> extends SettingsPropertyBase<T, Property<T>>
{
	private final T											defaultValue;
	private final BiFunction<XMLConfiguration, String, T>	loader;
	private final TriConsumer<XMLConfiguration, String, T>	saver;

	public ObjectSettingsProperty(String key, T defaultValue, BiFunction<XMLConfiguration, String, T> loader, TriConsumer<XMLConfiguration, String, T> saver)
	{
		super(key);
		this.defaultValue = defaultValue;
		this.loader = loader;
		this.saver = saver;
	}

	@Override
	protected Property<T> createProperty(String name)
	{
		return new SimpleObjectProperty<>(this, name, defaultValue);
	}

	@Override
	public void load(XMLConfiguration cfg)
	{
		T val = loader.apply(cfg, key);
		original.setValue(val);
		current.setValue(val);
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		saver.accept(cfg, key, current.getValue());
	}

	public T getDefaultValue()
	{
		return defaultValue;
	}
}
