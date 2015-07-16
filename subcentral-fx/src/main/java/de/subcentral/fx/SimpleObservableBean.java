package de.subcentral.fx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class SimpleObservableBean implements Observable, ObservableBean
{
    private final ChangeListener<Object>       internalPropChangeListener    = initInternalPropChangeListener();
    private final ObservableList<Property<?>>  properties		     = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private final List<InvalidationListener>   externalInvalidationListeners = new CopyOnWriteArrayList<>();
    private final List<ChangeListener<Object>> externalChangeListeners	     = new CopyOnWriteArrayList<>();

    public SimpleObservableBean()
    {
	properties.addListener((ListChangeListener.Change<? extends Property<?>> c) -> {
	    while (c.next())
	    {
		if (c.wasAdded())
		{
		    for (Property<?> added : c.getAddedSubList())
		    {
			added.addListener(internalPropChangeListener);
		    }
		}
		else if (c.wasRemoved())
		{
		    for (Property<?> removedElem : c.getRemoved())
		    {
			removedElem.removeListener(internalPropChangeListener);
		    }
		}
	    }
	});
    }

    private ChangeListener<Object> initInternalPropChangeListener()
    {
	return new ChangeListener<Object>()
	{
	    @Override
	    public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue)
	    {
		invalidate();
		for (ChangeListener<Object> l : externalChangeListeners)
		{
		    l.changed(observable, oldValue, newValue);
		}
	    }
	};
    }

    protected void bind(Property<?>... properties)
    {
	this.properties.addAll(properties);
    }

    @Override
    public ObservableList<Property<?>> getProperties()
    {
	return properties;
    }

    @Override
    public void addListener(InvalidationListener listener)
    {
	externalInvalidationListeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener)
    {
	externalInvalidationListeners.remove(listener);
    }

    @Override
    public void addListener(ChangeListener<Object> listener)
    {
	externalChangeListeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener<Object> listener)
    {
	externalChangeListeners.remove(listener);
    }

    @Override
    public void invalidate()
    {
	for (InvalidationListener l : externalInvalidationListeners)
	{
	    l.invalidated(this);
	}
    }
}
