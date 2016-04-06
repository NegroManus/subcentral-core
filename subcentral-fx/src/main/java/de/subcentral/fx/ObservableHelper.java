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
	private final InvalidationListener			dependencyListener	= (Observable obsv) -> invalidate();
	// VERY IMPORTANT to use a IdentityHashSet here
	// because otherwise Observables like ListProperties cannot be identified if their content changed
	private final ObservableSet<Observable>		dependencies		= FXCollections.observableSet(Sets.newIdentityHashSet());
	private final List<InvalidationListener>	listeners			= new CopyOnWriteArrayList<>();

	public ObservableHelper()
	{
		this(ImmutableList.of());
	}

	public ObservableHelper(Observable... dependencies)
	{
		this(ImmutableList.copyOf(dependencies));
	}

	public ObservableHelper(Collection<? extends Observable> dependencies)
	{
		this.dependencies.addListener((SetChangeListener.Change<? extends Observable> c) ->
		{
			if (c.wasAdded())
			{
				c.getElementAdded().addListener(dependencyListener);
			}
			if (c.wasRemoved())
			{
				c.getElementRemoved().removeListener(dependencyListener);
			}
		});
		this.dependencies.addAll(dependencies);
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
}
