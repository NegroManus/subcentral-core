package de.subcentral.fx;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class BeanInvalidationSupport implements Observable
{
    private final List<InvalidationListener> listeners = new CopyOnWriteArrayList<>();

    private final Observable bean;

    public BeanInvalidationSupport(Observable bean)
    {
	this.bean = Objects.requireNonNull(bean, "bean");
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

    public void bind(Observable... props)
    {
	for (Observable prop : props)
	{
	    prop.addListener((Observable observable) -> invalidate());
	}
    }

    public void invalidate()
    {
	for (InvalidationListener l : listeners)
	{
	    l.invalidated(bean);
	}
    }
}
