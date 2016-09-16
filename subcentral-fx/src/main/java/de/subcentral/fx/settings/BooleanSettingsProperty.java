package de.subcentral.fx.settings;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public final class BooleanSettingsProperty extends SettingsPropertyBase<Boolean, BooleanProperty> {
	private static final Logger log = LogManager.getLogger(BooleanSettingsProperty.class);

	public BooleanSettingsProperty(String key, boolean initialValue) {
		super(key, (Object bean, String name) -> new SimpleBooleanProperty(bean, name, initialValue));
	}

	public boolean get() {
		return property.get();
	}

	public void set(boolean value) {
		property.set(value);
	}

	@Override
	public void load(ImmutableConfiguration cfg, boolean resetChanged) {
		try {
			property.set(cfg.getBoolean(key));
			if (resetChanged) {
				changed.set(false);
			}
		}
		catch (Exception e) {
			log.error("Exception while loading settings property [" + key + "] from configuration", e);
		}
	}

	@Override
	public void save(Configuration cfg, boolean resetChanged) {
		cfg.setProperty(key, property.get());
		if (resetChanged) {
			changed.set(false);
		}
	}
}
