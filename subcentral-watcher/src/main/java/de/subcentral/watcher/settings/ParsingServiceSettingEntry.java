package de.subcentral.watcher.settings;

import de.subcentral.core.parse.ParsingService;

public class ParsingServiceSettingEntry extends AbstractDeactivatableSettingEntry<ParsingService>
{
	public ParsingServiceSettingEntry(ParsingService parsingService, boolean enabled)
	{
		super(parsingService, enabled);
	}
}