package de.subcentral.watcher.settings;

import javafx.util.StringConverter;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;

public class CompatibilitySettingsItem extends SimpleDeactivatableSettingsItem<Compatibility>
{
	public CompatibilitySettingsItem(Compatibility value, boolean enabled)
	{
		super(value, enabled);
	}

	public static final StringConverter<CompatibilitySettingsItem> STRING_CONVERTER = initStringConverter();

	private static StringConverter<CompatibilitySettingsItem> initStringConverter()
	{
		return new StringConverter<CompatibilitySettingsItem>()
		{
			@Override
			public String toString(CompatibilitySettingsItem entry)
			{
				Compatibility c = entry.getItem();
				if (c instanceof CrossGroupCompatibility)
				{
					return ((CrossGroupCompatibility) c).toShortString();
				}
				else
				{
					return c.toString();
				}
			}

			@Override
			public CompatibilitySettingsItem fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

}