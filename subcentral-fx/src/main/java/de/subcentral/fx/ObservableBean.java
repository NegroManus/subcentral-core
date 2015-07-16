package de.subcentral.fx;

import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;

public interface ObservableBean extends Observable
{
    ObservableList<Property<?>> getProperties();

    void addListener(ChangeListener<Object> listener);

    void removeListener(ChangeListener<Object> listener);

    void invalidate();
}