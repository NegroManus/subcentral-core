package de.subcentral.watcher.settings;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractSettingEntry<T> implements SettingEntry<T>
{
    protected final T		    value;
    protected final BooleanProperty enabled;

    public AbstractSettingEntry(T value, boolean enabled)
    {
	this.value = Objects.requireNonNull(value, "value");
	this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
    }

    @Override
    public T getValue()
    {
	return value;
    }

    @Override
    public final BooleanProperty enabledProperty()
    {
	return this.enabled;
    }

    @Override
    public final boolean isEnabled()
    {
	return this.enabled.get();
    }

    @Override
    public final void setEnabled(final boolean enabled)
    {
	this.enabled.set(enabled);
    }

    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	{
	    return true;
	}
	if (obj == null)
	{
	    return false;
	}
	if (getClass().equals(obj.getClass()))
	{
	    AbstractSettingEntry<?> o = (AbstractSettingEntry<?>) obj;
	    return value.equals(o.value);
	}
	return false;
    }

    @Override
    public int hashCode()
    {
	return new HashCodeBuilder(71, 913).append(getClass()).append(value).toHashCode();
    }
}
