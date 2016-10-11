package de.subcentral.fx.settings;

import javafx.beans.property.Property;
import javafx.beans.value.WritableValue;

public interface SettingsProperty<T, P extends Property<T>> extends Settable, WritableValue<T> {
    public String getKey();

    public P property();
}
