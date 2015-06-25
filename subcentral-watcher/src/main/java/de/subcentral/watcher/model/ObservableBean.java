package de.subcentral.watcher.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ObservableBean implements Observable
{
    private final InternalInvalidationListener internalInvalidationListener = new InternalInvalidationListener();
    private final ObservableList<Observable>   dependencies		    = FXCollections.observableArrayList(new CopyOnWriteArrayList<>());
    private final List<InvalidationListener>   listeners		    = new CopyOnWriteArrayList<>();

    public ObservableBean()
    {
	dependencies.addListener((ListChangeListener.Change<? extends Observable> c) -> {
	    while (c.next())
	    {
		if (c.wasAdded())
		{
		    for (Observable added : c.getAddedSubList())
		    {
			added.addListener(internalInvalidationListener);
		    }
		}
		else if (c.wasRemoved())
		{
		    for (Observable removedElem : c.getRemoved())
		    {
			removedElem.removeListener(internalInvalidationListener);
		    }
		}
	    }
	});
    }

    protected void bind(Observable... properties)
    {
	dependencies.addAll(properties);
    }

    public ObservableList<Observable> getDependencies()
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
