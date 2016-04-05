package de.subcentral.settings;

import java.util.function.BiFunction;

import org.apache.commons.configuration2.XMLConfiguration;

import de.subcentral.core.util.TriConsumer;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ListSettingsProperty<T> extends SettingsPropertyBase<ObservableList<T>, ListProperty<T>>
{
	private final BiFunction<XMLConfiguration, String, ObservableList<T>>	loader;
	private final TriConsumer<XMLConfiguration, String, ObservableList<T>>	saver;

	public ListSettingsProperty(String key, BiFunction<XMLConfiguration, String, ObservableList<T>> loader, TriConsumer<XMLConfiguration, String, ObservableList<T>> saver)
	{
		super(key);
		this.loader = loader;
		this.saver = saver;
	}

	@Override
	protected ListProperty<T> createProperty(String name)
	{
		return new SimpleListProperty<>(this, name, FXCollections.observableArrayList());
	}

	@Override
	public void load(XMLConfiguration cfg)
	{
		ObservableList<T> val = loader.apply(cfg, key);
		original.setAll(val);
		current.setAll(val);
	}

	@Override
	public void save(XMLConfiguration cfg)
	{
		saver.accept(cfg, key, current.getValue());
	}
}
