package de.subcentral.fx.settings;

import java.util.Objects;
import java.util.function.BiFunction;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.Property;

public abstract class ObjectSettingsPropertyBase<T, P extends Property<T>> extends SettingsPropertyBase<T, P> {
	private static final Logger						log	= LogManager.getLogger(ObjectSettingsPropertyBase.class);
	private final ConfigurationPropertyHandler<T>	handler;

	protected ObjectSettingsPropertyBase(String key, BiFunction<Object, String, P> propertyCreator, ConfigurationPropertyHandler<T> handler) {
		super(key, propertyCreator);
		this.handler = Objects.requireNonNull(handler, "handler");
	}

	@Override
	public void load(ImmutableConfiguration cfg, boolean resetChanged) {
		try {
			property.setValue(loadValue(cfg));
			if (resetChanged) {
				changed.set(false);
			}
		}
		catch (Exception e) {
			log.error("Exception while loading settings property [" + key + "] from configuration", e);
		}
	}

	public T loadValue(ImmutableConfiguration cfg) {
		return handler.get(cfg, key);
	}

	@Override
	public void save(Configuration cfg, boolean resetChanged) {
		saveValue(cfg, property.getValue());
		if (resetChanged) {
			changed.set(false);
		}
	}

	public void saveValue(Configuration cfg, T value) {
		handler.add(cfg, key, value);
	}
}
