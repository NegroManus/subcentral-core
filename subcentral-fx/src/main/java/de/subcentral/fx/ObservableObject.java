package de.subcentral.fx;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.Sets;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ObservableObject implements Observable
{
	private final InternalInvalidationListener	internalInvalidationListener	= new InternalInvalidationListener();
	// VERY IMPORTANT to use a IdentityHashSet here
	// because otherwise Observables like ListProperties cannot be identified if their content changed
	private final ObservableSet<Observable>		dependencies					= FXCollections.observableSet(Sets.newIdentityHashSet());
	private final List<InvalidationListener>	listeners						= new CopyOnWriteArrayList<>();

	public ObservableObject()
	{
		dependencies.addListener((SetChangeListener.Change<? extends Observable> c) ->
		{
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
