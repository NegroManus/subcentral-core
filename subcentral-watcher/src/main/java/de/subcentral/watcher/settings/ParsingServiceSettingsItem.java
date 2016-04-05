package de.subcentral.watcher.settings;

import de.subcentral.core.parse.ParsingService;
import de.subcentral.fx.settings.SimpleDeactivatableSettingsItem;

public class ParsingServiceSettingsItem extends SimpleDeactivatableSettingsItem<ParsingService>
{
	public ParsingServiceSettingsItem(ParsingService parsingService, boolean enabled)
	{
		super(parsingService, enabled);
	}
}