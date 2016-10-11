package de.subcentral.fx.settings;

import java.util.Objects;
import java.util.function.BiFunction;

import com.google.common.base.MoreObjects;

import javafx.beans.Observable;
import javafx.beans.property.Property;

public abstract class SettingsPropertyBase<T, P extends Property<T>> extends SettableBase implements SettingsProperty<T, P> {
    protected final String key;
    protected final P      property;

    protected SettingsPropertyBase(String key, BiFunction<Object, String, P> propertyCreator) {
        this.key = Objects.requireNonNull(key, "key");
        this.property = Objects.requireNonNull(propertyCreator, "propertyCreator").apply(this, "property");
        this.helper.getDependencies().add(this.property);
        this.helper.addListener((Observable o) -> changed.set(true));

        // TODO only for debug, remove!
        // addListener((Observable o) ->
        // {
        // System.out.println(key + " invalidated: " + getValue());
        // });
        // changed.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) ->
        // {
        // System.out.println(key + " changed: " + oldValue + " -> " + newValue);
        // });
    }

    public String getKey() {
        return key;
    }

    @Override
    public P property() {
        return property;
    }

    @Override
    public T getValue() {
        return property.getValue();
    }

    @Override
    public void setValue(T value) {
        property.setValue(value);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("key", key).add("property", property).toString();
    }
}
