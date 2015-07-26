package de.subcentral.watcher.settings;

import javafx.beans.property.BooleanProperty;

public interface DeactivatableSettingEntry<T> extends SettingEntry<T>
{
			BooleanProperty enabledProperty();

	boolean isEnabled();

	void setEnabled(final boolean enabled);
}
