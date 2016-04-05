package de.subcentral.fx.settings;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class SimpleDeactivatableSettingsItem<T> extends SimpleSettingsItem<T> implements DeactivatableSettingsItem<T>
{
	protected final BooleanProperty enabled;

	public SimpleDeactivatableSettingsItem(T value, boolean enabled)
	{
		super(value);
		this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
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
			SimpleDeactivatableSettingsItem<?> o = (SimpleDeactivatableSettingsItem<?>) obj;
			return item.equals(o.item) && isEnabled() == o.isEnabled();
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(71, 913).append(getClass()).append(item).append(isEnabled()).toHashCode();
	}
}
