package de.subcentral.fx;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ObservableObject implements Observable
{
    private final InternalInvalidationListener internalInvalidationListener = new InternalInvalidationListener();
    private final ObservableSet<Observable>    dependencies		    = FXCollections.observableSet(new LinkedHashSet<>());
    private final List<InvalidationListener>   listeners		    = new CopyOnWriteArrayList<>();

    public ObservableObject()
    {
	dependencies.addListener((SetChangeListener.Change<? extends Observable> c) -> {
	    if (c.wasAdded())
	    {
		c.getElementAdded().addListener(internalInvalidationListener);
	    }
	    if (c.wasRemoved())
	    {
		c.getElementRemoved().removeListener(internalInvalidationListener);
	    }
	});
    }

    protected void bind(Observable... properties)
    {
	for (Observable prop : properties)
	{
	    dependencies.add(prop);
	}
    }

    public ObservableSet<Observable> getDependencies()
    {
	return dependencies;
    }

    @Override
    public void addListener(InvalidationListener listener)
    {
	listeners.add(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener)
    {
	listeners.remove(listener);
    }

    public void invalidate()
    {
	for (InvalidationListener l : listeners)
	{
	    l.invalidated(this);
	}
    }

    private class InternalInvalidationListener implements InvalidationListener
    {
	@Override
	public void invalidated(Observable observable)
	{
	    invalidate();
	}
    }
}
