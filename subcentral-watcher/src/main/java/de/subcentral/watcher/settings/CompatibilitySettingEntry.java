package de.subcentral.watcher.settings;

import javafx.util.StringConverter;
import de.subcentral.core.metadata.release.Compatibility;
import de.subcentral.core.metadata.release.CrossGroupCompatibility;

public class CompatibilitySettingEntry extends AbstractSettingEntry<Compatibility>
{
	public CompatibilitySettingEntry(Compatibility value, boolean enabled)
	{
		super(value, enabled);
	}

	public static final StringConverter<CompatibilitySettingEntry>	STRING_CONVERTER	= initStringConverter();

	private static StringConverter<CompatibilitySettingEntry> initStringConverter()
	{
		return new StringConverter<CompatibilitySettingEntry>()
		{
			@Override
			public String toString(CompatibilitySettingEntry entry)
			{
				Compatibility c = entry.getValue();
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
			public CompatibilitySettingEntry fromString(String string)
			{
				throw new UnsupportedOperationException();
			}
		};
	}

}