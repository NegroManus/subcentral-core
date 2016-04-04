package de.subcentral.settings;

import com.google.common.collect.ImmutableList;

public class SettingsUtil
{
	public static <V, T extends DeactivatableSettingEntry<V>> ImmutableList<V> getValuesOfEnabledSettingEntries(Iterable<T> entries)
	{
		ImmutableList.Builder<V> enabledEntries = ImmutableList.builder();
		for (T entry : entries)
		{
			if (entry.isEnabled())
			{
				enabledEntries.add(entry.getValue());
			}
		}
		return enabledEntries.build();
	}

	public SettingsUtil()
	{
		throw new AssertionError(getClass() + " is an utility class and therefore cannot be instantiated");
	}
}
