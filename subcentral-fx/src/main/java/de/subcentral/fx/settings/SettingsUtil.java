package de.subcentral.fx.settings;

import com.google.common.collect.ImmutableList;

public class SettingsUtil {
    public static <V, T extends DeactivatableSettingsItem<V>> ImmutableList<V> getValuesOfEnabledSettingEntries(Iterable<T> entries) {
        ImmutableList.Builder<V> enabledEntries = ImmutableList.builder();
        for (T entry : entries) {
            if (entry.isEnabled()) {
                enabledEntries.add(entry.getItem());
            }
        }
        return enabledEntries.build();
    }

    public SettingsUtil() {
        throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
    }
}
