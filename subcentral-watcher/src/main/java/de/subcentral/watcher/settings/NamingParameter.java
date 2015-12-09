package de.subcentral.watcher.settings;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;

public class NamingParameter implements Comparable<NamingParameter>
{
	private final ReadOnlyStringWrapper	key;
	private final BooleanProperty		value;

	public NamingParameter(String key, boolean value)
	{
		this.key = new ReadOnlyStringWrapper(this, "key", Objects.requireNonNull(key, "key"));
		this.value = new SimpleBooleanProperty(this, "value", value);
	}

	public String getKey()
	{
		return key.get();
	}

	public ReadOnlyStringProperty keyProperty()
	{
		return key.getReadOnlyProperty();
	}

	public boolean getValue()
	{
		return value.get();
	}

	public void setValue(boolean value)
	{
		this.value.set(value);
	}

	public BooleanProperty valueProperty()
	{
		return value;
	}

	// Object methods
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj instanceof NamingParameter)
		{
			return Objects.equals(key.get(), ((NamingParameter) obj).key.get());
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(19, 611).append(key.get()).toHashCode();
	}

	@Override
	public int compareTo(NamingParameter o)
	{
		// nulls first
		if (o == null)
		{
			return 1;
		}
		return this.key.get().compareToIgnoreCase(o.key.get());
	}
}