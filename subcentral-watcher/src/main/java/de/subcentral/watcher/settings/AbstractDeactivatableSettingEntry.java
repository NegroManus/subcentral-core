package de.subcentral.watcher.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class AbstractDeactivatableSettingEntry<T> extends AbstractSettingEntry<T>implements DeactivatableSettingEntry<T>
{
	protected final BooleanProperty enabled;

	public AbstractDeactivatableSettingEntry(T value, boolean enabled)
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
