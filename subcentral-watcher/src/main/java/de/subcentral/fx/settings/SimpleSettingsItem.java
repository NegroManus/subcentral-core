package de.subcentral.fx.settings;

import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SimpleSettingsItem<T> implements SettingsItem<T>
{
	protected final T value;

	public SimpleSettingsItem(T value)
	{
		this.value = Objects.requireNonNull(value, "value");
	}

	@Override
	public T getValue()
	{
		return value;
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
			SimpleSettingsItem<?> o = (SimpleSettingsItem<?>) obj;
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
