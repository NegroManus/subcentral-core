package de.subcentral.fx.settings;

import javafx.beans.property.BooleanProperty;

public interface DeactivatableSettingsItem<T> extends SettingsItem<T> {
	BooleanProperty enabledProperty();

	boolean isEnabled();

	void setEnabled(final boolean enabled);
}
