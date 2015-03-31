package de.subcentral.watcher.settings;

import javafx.beans.property.BooleanProperty;

public interface SettingEntry<T>
{
	T getValue();

	BooleanProperty enabledProperty();

	boolean isEnabled();

	void setEnabled(final boolean enabled);
}
