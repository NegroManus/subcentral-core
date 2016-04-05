package de.subcentral.watcher.settings;

import de.subcentral.core.parse.ParsingService;
import de.subcentral.settings.DeactivatableSettingsItemBase;

public class ParsingServiceSettingEntry extends DeactivatableSettingsItemBase<ParsingService>
{
	public ParsingServiceSettingEntry(ParsingService parsingService, boolean enabled)
	{
		super(parsingService, enabled);
	}
}