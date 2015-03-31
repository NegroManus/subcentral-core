package de.subcentral.watcher.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import de.subcentral.core.metadata.release.Compatibility;

public class CompatibilitySettingEntry implements SettingEntry<Compatibility>
{
	private final Compatibility		compatibility;
	private final BooleanProperty	enabled;

	public CompatibilitySettingEntry(Compatibility compatibility, boolean enabled)
	{
		this.compatibility = compatibility;
		this.enabled = new SimpleBooleanProperty(this, "enabled", enabled);
	}

	@Override
	public Compatibility getValue()
	{
		return compatibility;
	}

	@Override
	public final BooleanProperty enabledProperty()
	{
		return this.enabled;
	}

	@Override
	public final boolean isEnabled()
	{
		return this.enabledProperty().get();
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		this.enabledProperty().set(enabled);
	}
}