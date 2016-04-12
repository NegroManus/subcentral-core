package de.subcentral.fx;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public final class ObservableHelper implements Observable
{
	private final InvalidationListener			dependencyListener	= (Observable obsv) -> fireInvalidationEvent();
	private final Observable					observable;
	// VERY IMPORTANT to use a IdentityHashSet here
	// because otherwise Observables like ListProperties cannot be identified if their content changed
	private final ObservableSet<Observable>		dependencies		= FXCollections.observableSet(Sets.newIdentityHashSet());
	private final List<InvalidationListener>	listeners			= new CopyOnWriteArrayList<>();

	public ObservableHelper(Observable observable)
	{
		this(observable, ImmutableList.of());
	}

	public ObservableHelper(Observable observable, Observable... dependencies)
	{
		this(observable, ImmutableList.copyOf(dependencies));
	}

	public ObservableHelper(Observable observable, Collection<? extends Observable> dependencies)
	{
		this.observable = observable != null ? observable : this;
		this.dependencies.addListener((SetChangeListener.Change<? extends Observable> c) ->
		{
			if (c.wasAdded() && c.getElementAdded() != null)
			{
				c.getElementAdded().addListener(dependencyListener);
			}
			if (c.wasRemoved() && c.getElementRemoved() != null)
			{
				c.getElementRemoved().removeListener(dependencyListener);
			}
		});
		this.dependencies.addAll(dependencies);
	}

	public Observable getObservable()
	{
		return observable;
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

	private void fireInvalidationEvent()
	{
		for (InvalidationListener l : listeners)
		{
			l.invalidated(observable);
		}
	}
}
