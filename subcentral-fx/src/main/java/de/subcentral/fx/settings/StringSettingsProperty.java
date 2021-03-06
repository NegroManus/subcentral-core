package de.subcentral.fx.settings;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class StringSettingsProperty extends SettingsPropertyBase<String, StringProperty> {
    private static final Logger log = LogManager.getLogger(StringSettingsProperty.class);

    public StringSettingsProperty(String key) {
        this(key, null);
    }

    public StringSettingsProperty(String key, String initialValue) {
        super(key, (Object bean, String name) -> new SimpleStringProperty(bean, name, initialValue));
    }

    @Override
    public void load(ImmutableConfiguration cfg, boolean resetChanged) {
        try {
            property.set(cfg.getString(key));
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
