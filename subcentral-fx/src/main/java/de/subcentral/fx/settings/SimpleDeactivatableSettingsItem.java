package de.subcentral.fx.settings;

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
}
