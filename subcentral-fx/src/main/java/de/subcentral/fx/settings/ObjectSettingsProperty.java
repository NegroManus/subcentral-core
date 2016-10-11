package de.subcentral.fx.settings;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

public final class ObjectSettingsProperty<T> extends ObjectSettingsPropertyBase<T, Property<T>> {
    public ObjectSettingsProperty(String key, ConfigurationPropertyHandler<T> handler) {
        this(key, handler, null);
    }

    public ObjectSettingsProperty(String key, ConfigurationPropertyHandler<T> handler, T initialValue) {
        super(key, (Object bean, String name) -> new SimpleObjectProperty<T>(bean, name, initialValue), handler);
    }
}
